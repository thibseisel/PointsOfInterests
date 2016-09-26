package fr.nihilus.pointofinterests;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.Locale;

public class POIMainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int ADD_REQUEST_CODE = 16;
    public static final int EDIT_REQUEST_CODE = 56;
    private POICursorAdapter mAdapter;
    private int mCheckedPosition = ListView.INVALID_POSITION;
    private ActionMode mActionMode;

    /**
     * Menu contextuel apparaissant par dessus l'ActionBar lorsqu'un click long est effectué
     * sur un élément de la liste.
     */
    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            mActionMode = mode;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return onOptionsItemSelected(item);
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mCheckedPosition = ListView.INVALID_POSITION;
            mActionMode = null;
        }
    };

    private View mEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poimain);

        ListView listView = (ListView) findViewById(android.R.id.list);
        mAdapter = new POICursorAdapter(this, null);
        listView.setAdapter(mAdapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long id) {
                mCheckedPosition = pos;
                startSupportActionMode(mActionModeCallback);
                return true;
            }
        });

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toAddActivity = new Intent(POIMainActivity.this, POIEditorActivity.class);
                startActivityForResult(toAddActivity, ADD_REQUEST_CODE);
            }
        });

        // Démarre le chargement asynchrone des PointOfInterest depuis la base de données.
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_map:
                PointOfInterest in = mAdapter.getPointOfInterest(mCheckedPosition);
                String mapsUri = String.format(Locale.US, "geo:%f,%f", in.getLatitude(), in.getLongitude());
                Intent toGMaps = new Intent(Intent.ACTION_VIEW, Uri.parse(mapsUri));
                startActivity(toGMaps);
                break;
            case R.id.action_share:
                PointOfInterest sharedPoi = mAdapter.getPointOfInterest(mCheckedPosition);
                Intent sharingIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("sms:"));
                sharingIntent.putExtra("sms_body", sharedPoi.toString());
                startActivity(sharingIntent);
                break;
            case R.id.action_edit:
                Intent editIntent = new Intent(this, POIEditorActivity.class);
                PointOfInterest editedPoi = mAdapter.getPointOfInterest(mCheckedPosition);
                editIntent.putExtra(POIEditorActivity.EXTRA_POI, editedPoi);
                startActivityForResult(editIntent, EDIT_REQUEST_CODE);
                break;
            case R.id.action_delete:
                getContentResolver().delete(
                        ContentUris.withAppendedId(InterestProvider.CONTENT_URI,
                                mAdapter.getItemId(mCheckedPosition)), null, null);
                mActionMode.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, InterestProvider.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mEmptyView == null && data.getCount() == 0) {
            mEmptyView = ((ViewStub) findViewById(android.R.id.empty)).inflate();
        } else if (data.getCount() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else if (mEmptyView != null) {
            mEmptyView.setVisibility(View.GONE);
        }

        mAdapter.swapCursor(data);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
        mAdapter.notifyDataSetInvalidated();
    }
}
