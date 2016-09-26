package fr.nihilus.pointofinterests;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.CursorAdapter;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static fr.nihilus.pointofinterests.InterestDatabase.COL_DATE;
import static fr.nihilus.pointofinterests.InterestDatabase.COL_DESCR;
import static fr.nihilus.pointofinterests.InterestDatabase.COL_LABEL;
import static fr.nihilus.pointofinterests.InterestDatabase.COL_LAT;
import static fr.nihilus.pointofinterests.InterestDatabase.COL_LONG;
import static fr.nihilus.pointofinterests.InterestDatabase.COL_RATING;

/**
 *
 */
class POICursorAdapter extends CursorAdapter {

    private int colLabel;
    private int colDescr;
    private int colDate;
    private int colRating;
    private int colLatitude;
    private int colLongitude;

    POICursorAdapter(@NonNull Context context, @Nullable Cursor c) {
        super(context, c, 0);
        if(c != null) {
            setColumns(c);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rootView = inflater.inflate(R.layout.poi_list_item, parent, false);
        POIHolder holder = new POIHolder(rootView);
        rootView.setTag(holder);
        return rootView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        POIHolder holder = (POIHolder) view.getTag();
        holder.label.setText(cursor.getString(colLabel));
        holder.descr.setText(cursor.getString(colDescr));
        holder.setRating(cursor.getFloat(colRating));
        holder.setTimeStr(cursor.getLong(colDate));
        holder.latitude = cursor.getDouble(colLatitude);
        holder.longitude = cursor.getDouble(colLongitude);
    }

    /**
     * Met en cache l'index des colonnes dans le {@link Cursor}.
     */
    private void setColumns(Cursor c) {
        colLabel = c.getColumnIndexOrThrow(COL_LABEL);
        colDescr = c.getColumnIndexOrThrow(COL_DESCR);
        colDate = c.getColumnIndexOrThrow(COL_DATE);
        colLatitude = c.getColumnIndexOrThrow(COL_LAT);
        colLongitude = c.getColumnIndexOrThrow(COL_LONG);
        colRating = c.getColumnIndexOrThrow(COL_RATING);
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        if(newCursor != null) {
            setColumns(newCursor);
        }
        return super.swapCursor(newCursor);
    }

    /**
     * Récupère une référence à un {@link PointOfInterest} à partir du {@link Cursor}.
     */
    PointOfInterest getPointOfInterest(int position) {
        Cursor c = getCursor();
        if (!c.moveToPosition(position)) return null;
        return new PointOfInterest(c);
    }

    /**
     * ViewHolder pattern, permettant de garder une référence aux View qui composent l'item
     * de la ListView.
     */
    private static class POIHolder {
        TextView label;
        TextView descr;
        TextView date;
        TextView rating;

        double latitude;
        double longitude;

        POIHolder(View root) {
            label = (TextView) root.findViewById(R.id.label);
            descr = (TextView) root.findViewById(R.id.description);
            date = (TextView) root.findViewById(R.id.date);
            rating = (TextView) root.findViewById(R.id.rating);
        }

        /**
         * Transcrit la note (entre 0 et 5) en une chaine de caractères.
         */
        void setRating(float rank) {
            rating.setText(String.valueOf(rank));
        }

        void setTimeStr(long millis) {
            CharSequence timeStr = DateUtils.getRelativeTimeSpanString(millis,
                    System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE);
            date.setText(timeStr);
        }
    }
}
