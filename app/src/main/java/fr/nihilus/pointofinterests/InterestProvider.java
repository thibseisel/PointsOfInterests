package fr.nihilus.pointofinterests;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import static fr.nihilus.pointofinterests.InterestDatabase.COL_DATE;
import static fr.nihilus.pointofinterests.InterestDatabase.TABLE_POI;

/**
 * Un {@link ContentProvider} permettant la lecture, l'ajout, la mise à jour et la suppression
 * de {@link PointOfInterest} dans la base de données locale {@link InterestDatabase}.
 * Les requêtes sont effectuées à partir d'URIs spécifiques (comme pour les services REST).
 * L'utilisation de cette couche d'abstraction pour l'accès aux données permet d'utiliser un
 * {@link android.support.v4.content.CursorLoader} pour récupérer les données de manière asynchrone
 * et de recharger automatiquement les données lorsqu'elles ont changé.
 */
@SuppressWarnings("ConstantConditions")
public class InterestProvider extends ContentProvider {

    public static final int ALL_POI = 10;
    public static final int POI_ID = 11;
    public static final String AUTHORITY = "fr.nihilus.pointofinterests.provider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
    public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TABLE_POI);

    public static final String DEFAULT_SORT_ORDER = COL_DATE + " ASC";
    private static final String TAG = "InterestProvider";
    static UriMatcher sMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sMatcher.addURI(AUTHORITY, TABLE_POI, ALL_POI);
        sMatcher.addURI(AUTHORITY, TABLE_POI + "/#", POI_ID);
    }

    private InterestDatabase mHelper;

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mHelper.getWritableDatabase();
        int deleteCount = 0;
        switch (sMatcher.match(uri)) {
            case ALL_POI:
                deleteCount = db.delete(TABLE_POI, selection, selectionArgs);
                break;
            case POI_ID:
                deleteCount = deleteWithId(uri, selection, selectionArgs, db);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return deleteCount;
    }

    private int deleteWithId(Uri uri, String selection, String[] selectionArgs, SQLiteDatabase db) {
        int deleteCount;
        String whereId = BaseColumns._ID + "=" + uri.getLastPathSegment();
        if(TextUtils.isEmpty(selection)) {
            deleteCount = db.delete(TABLE_POI, whereId, null);
        } else {
            deleteCount = db.delete(TABLE_POI, whereId + " AND " + selection, selectionArgs);
        }
        return deleteCount;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        if (sMatcher.match(uri) != ALL_POI) {
            throw new UnsupportedOperationException("Unsupported URI: " + uri);
        }

        final SQLiteDatabase db = mHelper.getWritableDatabase();
        long insertId = db.insertOrThrow(TABLE_POI, null, values);
        Log.d(TAG, "insert: insertId=" + insertId);
        getContext().getContentResolver().notifyChange(uri, null);

        return insertId != -1 ? ContentUris.withAppendedId(uri, insertId) : null;
    }

    @Override
    public boolean onCreate() {
        mHelper = new InterestDatabase(getContext());
        return false;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor;
        switch (sMatcher.match(uri)) {
            case ALL_POI:
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = DEFAULT_SORT_ORDER;
                }
                cursor = db.query(TABLE_POI, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case POI_ID:
                String whereId = BaseColumns._ID + "=" + uri.getLastPathSegment();
                cursor = db.query(TABLE_POI, projection, whereId, null, null, null, null);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported URI: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        final SQLiteDatabase db = mHelper.getWritableDatabase();
        int updateCount;
        switch (sMatcher.match(uri)) {
            case ALL_POI:
                updateCount = db.update(TABLE_POI, values, selection, selectionArgs);
                break;
            case POI_ID:
                String id = uri.getLastPathSegment();
                updateCount = updateWithId(uri, values, selection, selectionArgs, db);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return updateCount;
    }

    private int updateWithId(Uri uri, ContentValues values, String selection,
                             String[] selectionArgs, SQLiteDatabase db) {
        int updateCount;
        String whereId = BaseColumns._ID + "=" + uri.getLastPathSegment();
        if(TextUtils.isEmpty(selection)) {
            updateCount = db.update(TABLE_POI, values, whereId, null);
        } else {
            updateCount = db.update(TABLE_POI, values,
                    whereId + " AND " + selection, selectionArgs);
        }
        return updateCount;
    }
}
