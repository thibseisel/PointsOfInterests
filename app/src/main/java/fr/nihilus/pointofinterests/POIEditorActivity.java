package fr.nihilus.pointofinterests;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.Date;

/**
 * Activité permettant la création et l'édition d'un PointOfInterest.
 */
public class POIEditorActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int PERMISSION_REQUEST_CODE = 42;
    public static final String EXTRA_POI = "PointOfInterest";
    public static final int SETTINGS_REQUEST_CODE = 32;
    private static final String TAG = "POIEditorActivity";
    private TextInputEditText mLabelText;
    private TextInputEditText mDescriptionText;
    private TextView mLocationButton;
    private RatingBar mRatingBar;
    private boolean mIsInEditMode;
    private Location mCurrentLocation;
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location == null) {
                Log.w(TAG, "onLocationChanged: no location.");
                Snackbar.make(findViewById(R.id.parent), R.string.gps_disabled, Snackbar.LENGTH_LONG)
                        .setAction("Enable", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent toSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivityForResult(toSettings, SETTINGS_REQUEST_CODE);
                            }
                        }).show();
                return;
            }
            mCurrentLocation = location;
            Log.d(TAG, "onLocationChanged: location=" + location.toString());
            mLocationButton.setText(PointOfInterest.locationString(location.getLatitude(),
                    location.getLongitude()));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // Ne rien faire
        }

        @Override
        public void onProviderEnabled(String provider) {
            // Ne rien faire
        }

        @Override
        public void onProviderDisabled(String provider) {
            // Ne rien faire
        }
    };
    private LocationManager mLocationManager;
    private long mEditId;

    private static boolean labelIsValid(String label) {
        return !TextUtils.isEmpty(label);
    }

    private static boolean locationIsValid(Location loc) {
        return loc != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poieditor);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLabelText = (TextInputEditText) findViewById(R.id.label);
        mDescriptionText = (TextInputEditText) findViewById(R.id.description);
        mLocationButton = (TextView) findViewById(R.id.current_position);
        mRatingBar = (RatingBar) findViewById(R.id.rating);

        mLocationButton.setOnClickListener(this);

        Intent startIntent = getIntent();
        PointOfInterest poi = (PointOfInterest) startIntent.getSerializableExtra(EXTRA_POI);

        if (poi != null) {
            // Référence à un PointOfInterest existant : mode édition
            setTitle(R.string.edit_poi);
            mIsInEditMode = true;
            mEditId = poi.getId();
            mLabelText.setText(poi.getLabel());
            mDescriptionText.setText(poi.getDescription());
            mRatingBar.setRating(poi.getRating());
            mCurrentLocation = new Location(LocationManager.GPS_PROVIDER);
            mCurrentLocation.setLatitude(poi.getLatitude());
            mCurrentLocation.setLongitude(poi.getLongitude());
            mLocationButton.setText(poi.getLocationText());

            Log.d(TAG, "onCreate: mEditId=" + mEditId);
        } else {
            // Aucune référence à un PointOfInterest existant : mode ajout
            setTitle(R.string.new_poi);
            mIsInEditMode = false;
            mLocationButton.setText(R.string.update_location);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (PERMISSION_REQUEST_CODE == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                requestCurrentLocation();
            }
        }
    }

    @SuppressWarnings("MissingPermission")
    private void requestCurrentLocation() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
        mLocationManager.requestSingleUpdate(mLocationManager.getBestProvider(criteria, true),
                mLocationListener, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.action_done == item.getItemId()) {
            save();
        }
        return super.onOptionsItemSelected(item);
    }

    private void save() {
        PointOfInterest newPoi = new PointOfInterest();
        String label = mLabelText.getText().toString();

        // Vérification de la saisie
        if (!labelIsValid(label)) {
            Snackbar.make(findViewById(R.id.parent), R.string.empty_label,
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        newPoi.setLabel(label);
        newPoi.setDescription(mDescriptionText.getText().toString());
        newPoi.setRating(mRatingBar.getRating());

        // La localisation doit être prête
        if (!locationIsValid(mCurrentLocation)) {
            Snackbar.make(findViewById(R.id.parent), R.string.location_not_ready,
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        newPoi.setLatitude(mCurrentLocation.getLatitude());
        newPoi.setLongitude(mCurrentLocation.getLongitude());
        newPoi.setDate(new Date());

        ContentValues cv = PointOfInterest.convertToValues(newPoi);
        if(mIsInEditMode) {
            getContentResolver().update(ContentUris
                    .withAppendedId(InterestProvider.CONTENT_URI, mEditId), cv, null, null);
        } else {
            getContentResolver().insert(InterestProvider.CONTENT_URI, cv);
        }

        Intent data = new Intent();
        data.setAction("fr.nihilus.pointofinterests.ACTION_ADD");
        data.putExtra(EXTRA_POI, newPoi);
        setResult(RESULT_OK, data);

        finish();
    }

    @Override
    public void onClick(View view) {
        if (R.id.current_position == view.getId()) {
            if (!PermissionUtil.hasLocationPermissions(this)) {
                PermissionUtil.requestLocationPermissions(this, PERMISSION_REQUEST_CODE);
            } else {
                requestCurrentLocation();
            }
        }
    }
}
