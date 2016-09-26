package fr.nihilus.pointofinterests;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Classe permettant la connexion avec la base de données locale.
 * La base de données est créée si elle n'existe pas.
 * Pour effectuer des opérations sur la base de données, voir {@link InterestProvider}.
 */
class InterestDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "PointOfInterests.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_POI = "poi";

    public static final String COL_LABEL = "label";
    public static final String COL_DESCR = "descr";
    public static final String COL_LAT = "latitude";
    public static final String COL_LONG = "longitude";
    public static final String COL_DATE = "date";
    public static final String COL_RATING = "rating";

    public InterestDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_POI + " (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY, " +
                COL_LABEL + " TEXT NOT NULL, " +
                COL_DESCR + " TEXT, " +
                COL_LAT + " REAL NOT NULL, " +
                COL_LONG + " REAL NOT NULL, " +
                COL_DATE + " INTEGER NOT NULL, " +
                COL_RATING + " REAL" + ");";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // Do nothing
    }
}
