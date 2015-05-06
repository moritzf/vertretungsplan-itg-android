package de.itgdah.vertretungsplan.web;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.SimpleCursorAdapter;

import org.jsoup.nodes.Document;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import de.itgdah.vertretungsplan.data.VertretungsplanContract;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.Days;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.Vertretungen;
import de.itgdah.vertretungsplan.Utility;

/**
 * Created by moritz on 24.03.15.
 */
public class FetchVertretungsplanTask extends AsyncTask<Void, Void, Cursor>{
    private final String LOG_TAG = "asyncTask";

    private final SimpleCursorAdapter adapter;
    private final Context mContext;

    public FetchVertretungsplanTask(SimpleCursorAdapter adapter,
                                    Context context) {
        this.adapter = adapter;
        mContext = context;
    }

    /**
     * Inserts a new date into the days database table.
     * @param date format: yyyyMMdd
     * @return the row ID of the added date
     */
    private long addDate(String date) {
       Cursor cursor = mContext.getContentResolver().query
               (Days.CONTENT_URI, new String[] {Days._ID},
                       Days.COLUMN_DATE + " = ?", new String[]{date},
                       null);
        if(cursor.moveToFirst()) {
            int dateIdIndex = cursor.getColumnIndex(Days._ID);
            return cursor.getLong(dateIdIndex);
        } else {
            ContentValues daysValue = new ContentValues();
            daysValue.put(Days.COLUMN_DATE, date);

            Uri dateInsertUri = mContext.getContentResolver().insert(Days
                    .CONTENT_URI, daysValue);

            return ContentUris.parseId(dateInsertUri);
        }
    }

    /**
     * Inserts a new entry into the vertretungsplan database table. Assumes that the associated
     * date of the entry, e.g. 20151105, is already present in the database.
     * @param entry Array containing all columns. Refer to {@link VertretungsplanParser} for
     *              further information.
     * @param date the date which is associated with the entry.
     * @return the row Id of the added entry
     */
    private long addEntry(String[] entry, String date) {
        long dateId = addDate(date);
        /* Assumes that the date is already present in the database
        // and therefore just returns the row id. The date row id is then used as a foreign key
        in the vertretungsplan table. */
        final String addEntrySelection = "";
        final String[] addEntrySelectionArgs = new String[] {
                    Long.toString(dateId),
                    entry[0],
                    entry[1],
                    entry[2],
                    entry[3],
                    entry[4],
                    entry[5],
        };
        Cursor cursor = mContext.getContentResolver().query(Vertretungen.CONTENT_URI, new
                String[] {Vertretungen._ID}, addEntrySelection, addEntrySelectionArgs, null);
        if (cursor.moveToFirst()) {
            int entryIdIndex = cursor.getColumnIndex(Days._ID);
            return cursor.getLong(entryIdIndex);
        }
    }

    @Override
    protected Cursor doInBackground(Void... params) {
        String[] vertretungenArray = null;
        VertretungsplanParser parser = new VertretungsplanParser();
        try {
            Document doc = parser.getDocumentViaLogin(VertretungsplanParser.URL_VERTRETUNGSPLAN);
            String[] dates = parser.getAvailableVertretungsplaeneDates(doc);
            HashMap<String, ArrayList<String[]>> vertretungsplanMap = parser.getVertretungsplan(doc);
            ArrayList<String[]> vertretungsplan = vertretungsplanMap.get(dates[0]);
            Log.v("async", "connection established");
            DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMAN);
            if (dates != null) {
                for(String date : dates) {
                    Date dateObj = dateFormat.parse(date, new ParsePosition(4));
                    addDate(VertretungsplanContract.convertDateToDatabaseFriendlyFormat(dateObj));
                }

            }
            if (vertretungsplan != null) {
               for (String[] entry : vertretungsplan) {
                   addEntry(entry, dates[0]);
               }
            }
        } catch (Exception e) {
            Log.e("Error", "error");
        }
        return mContext.getContentResolver().query(VertretungsplanContract.Vertretungen.CONTENT_URI,
                null, null, null, null );
    }

    protected void onPostExecute(Cursor cursor) {
        if (cursor != null) {
           Log.v(LOG_TAG, "Count dates " + Integer.toString(cursor.getCount()));
            DatabaseUtils.dumpCursor(cursor);
            adapter.swapCursor(cursor);

        }
    }
}

