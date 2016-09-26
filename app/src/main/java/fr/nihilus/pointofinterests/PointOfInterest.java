package fr.nihilus.pointofinterests;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;

import static fr.nihilus.pointofinterests.InterestDatabase.COL_DATE;
import static fr.nihilus.pointofinterests.InterestDatabase.COL_DESCR;
import static fr.nihilus.pointofinterests.InterestDatabase.COL_LABEL;
import static fr.nihilus.pointofinterests.InterestDatabase.COL_LAT;
import static fr.nihilus.pointofinterests.InterestDatabase.COL_LONG;
import static fr.nihilus.pointofinterests.InterestDatabase.COL_RATING;

public class PointOfInterest implements Serializable {

    public static final int UNKNOWN_ID = -1;

    private long id = UNKNOWN_ID;
    private String label;
    private String description;
    private double latitude;
    private double longitude;
    private Date date;
    private float rating;

    public PointOfInterest() {
        // Empty constructor
    }

    public PointOfInterest(Cursor c) {
        id = c.getLong(c.getColumnIndex(BaseColumns._ID));
        label = c.getString(c.getColumnIndex(COL_LABEL));
        description = c.getString(c.getColumnIndex(COL_DESCR));
        latitude = c.getDouble(c.getColumnIndex(COL_LAT));
        longitude = c.getDouble(c.getColumnIndex(COL_LONG));
        date = new Date(c.getLong(c.getColumnIndex(COL_DATE)));
        rating = c.getFloat(c.getColumnIndex(COL_RATING));
    }

    public long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getLocationText() {
        return locationString(latitude, longitude);
    }

    @Override
    public String toString() {
        return label + '\n'
                + description + '\n'
                + locationString(latitude, longitude);
    }

    public static String locationString(double latitude, double longitude) {
        return String.format(Locale.US, "%.2f, %.2f", latitude, longitude);
    }

    public static ContentValues convertToValues(PointOfInterest poi) {
        ContentValues cv = new ContentValues(7);
        cv.put(COL_LABEL, poi.label);
        cv.put(COL_DESCR, poi.description);
        cv.put(COL_LAT, poi.latitude);
        cv.put(COL_LONG, poi.longitude);
        cv.put(COL_DATE, poi.date.getTime());
        cv.put(COL_RATING, poi.rating);
        return cv;
    }
}
