package de.itgdah.vertretungsplan.web;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

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
import de.itgdah.vertretungsplan.data.VertretungsplanContract.AbsentClasses;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.GeneralInfo;

/**
 * Created by moritz on 24.03.15.
 */
public class FetchVertretungsplanTask extends AsyncTask<Void, Void, Void> {

    private final String LOG_TAG = FetchVertretungsplanTask.class.getSimpleName();

    private final Context mContext;

    public FetchVertretungsplanTask(Context context) {
        mContext = context;
    }

    /**
     * Inserts a new date into the days database table.
     *
     * @param date format: yyyyMMdd
     * @return the row ID of the added date
     */
    private long addDate(String date) {
        Cursor cursor = mContext.getContentResolver().query
                (Days.CONTENT_URI, new String[]{Days._ID},
                        Days.COLUMN_DATE + " = ?", new String[]{date},
                        null);
        if (cursor.moveToFirst()) {
            int dateIdIndex = cursor.getColumnIndex(Days._ID);
            return cursor.getLong(dateIdIndex);
        } else {
            ContentValues dayValue = new ContentValues();
            dayValue.put(Days.COLUMN_DATE, date);

            Uri dateInsertUri = mContext.getContentResolver().insert(Days
                    .CONTENT_URI, dayValue);

            return ContentUris.parseId(dateInsertUri);
        }
    }

    /**
     * Inserts a new entry into the vertretungsplan database table. Assumes that the associated
     * date of the entry, e.g. 20151105, is already present in the database.
     *
     * @param entry Array containing all columns. Refer to {@link VertretungsplanParser} for
     *              further information.
     * @param date  the date which is associated with the entry.
     * @return the row Id of the added entry
     */
    private long addVertretungsplanEntry(String[] entry, String date) {
        long dateId = getDateId(date);
        /* The date row id is used
        as a foreign key in the vertretungsplan table. */
        final String addEntrySelection = Vertretungen.COLUMN_DAYS_KEY + " = ? AND " +
                Vertretungen.COLUMN_PERIOD + " = ? AND " + Vertretungen.COLUMN_CLASS + " = ? AND" +
                " " + Vertretungen.COLUMN_SUBJECT + " = ? AND " + Vertretungen
                .COLUMN_VERTRETEN_DURCH + " = ? AND " + Vertretungen
                .COLUMN_ROOM + " = " + "? AND " + Vertretungen.COLUMN_COMMENT + " = ?";
        final String[] addEntrySelectionArgs = new String[]{
                Long.toString(dateId),
                entry[0],
                entry[1],
                entry[2],
                entry[3],
                entry[4],
                entry[5],
        };
        Cursor cursor = mContext.getContentResolver().query(Vertretungen.CONTENT_URI, new
                String[]{Vertretungen._ID}, addEntrySelection, addEntrySelectionArgs, null);
        if (cursor.moveToFirst()) {
            int entryIdIndex = cursor.getColumnIndex(Vertretungen._ID);
            return cursor.getLong(entryIdIndex);
        } else {
            ContentValues entryValues = new ContentValues();
            entryValues.put(Vertretungen.COLUMN_DAYS_KEY, dateId);
            entryValues.put(Vertretungen.COLUMN_PERIOD, entry[0]);
            entryValues.put(Vertretungen.COLUMN_CLASS, entry[1]);
            entryValues.put(Vertretungen.COLUMN_SUBJECT, entry[2]);
            entryValues.put(Vertretungen.COLUMN_VERTRETEN_DURCH, entry[3]);
            entryValues.put(Vertretungen.COLUMN_ROOM, entry[4]);
            entryValues.put(Vertretungen.COLUMN_COMMENT, entry[5]);

            Uri vertretungenInsertUri = mContext.getContentResolver().insert(Vertretungen
                    .CONTENT_URI, entryValues);
            return ContentUris.parseId(vertretungenInsertUri);
        }
    }

    /**
     * Adds a general info line to the database.
     *
     * @param generalInfo String containing one line of the general information.
     * @param date        the date associated with the general info.
     * @return the row id of the added entry
     */
    private long addGeneralInfoEntry(String generalInfo, String date) {
        long dateId = getDateId(date);
        /* The date row id is used
        as a foreign key in the general info table. */
        final String addEntrySelection = GeneralInfo.COLUMN_DAYS_KEY + " = ? AND " + GeneralInfo
                .COLUMN_MESSAGE + " = ?";
        final String[] addEntrySelectionArgs = new String[]{
                Long.toString(dateId),
                generalInfo
        };
        Cursor cursor = mContext.getContentResolver().query(GeneralInfo.CONTENT_URI, new
                String[]{GeneralInfo._ID}, addEntrySelection, addEntrySelectionArgs, null);
        if (cursor.moveToFirst()) {
            int entryIdIndex = cursor.getColumnIndex(GeneralInfo._ID);
            return cursor.getLong(entryIdIndex);
        } else {
            ContentValues entryValues = new ContentValues();
            entryValues.put(GeneralInfo.COLUMN_MESSAGE, generalInfo);
            Uri generalInfoInsertUri = mContext.getContentResolver().insert(GeneralInfo
                    .CONTENT_URI, entryValues);
            return ContentUris.parseId(generalInfoInsertUri);
        }
    }

    /**
     * Adds a general info line to the database.
     *
     * @param absentClass String containing one absent class.
     * @param date        the date associated with the general info.
     * @return the row id of the added entry
     */
    private long addAbsentClassesEntry(String absentClass, String date) {
        long dateId = getDateId(date);
        /* The date row id is used
        as a foreign key in the absent classes table. */
        final String addEntrySelection = AbsentClasses.COLUMN_DAYS_KEY + " = ? AND " + AbsentClasses
                .COLUMN_MESSAGE + " = ?";
        final String[] addEntrySelectionArgs = new String[]{
                Long.toString(dateId),
                absentClass
        };
        Cursor cursor = mContext.getContentResolver().query(AbsentClasses.CONTENT_URI, new
                String[]{AbsentClasses._ID}, addEntrySelection, addEntrySelectionArgs, null);
        if (cursor.moveToFirst()) {
            int entryIdIndex = cursor.getColumnIndex(AbsentClasses._ID);
            return cursor.getLong(entryIdIndex);
        } else {
            ContentValues entryValues = new ContentValues();
            entryValues.put(AbsentClasses.COLUMN_MESSAGE, absentClass);
            Uri absentClassesInsertUri = mContext.getContentResolver().insert(AbsentClasses
                    .CONTENT_URI, entryValues);
            return ContentUris.parseId(absentClassesInsertUri);
        }
    }

    /**
     * Gets the date id in the days table of the specified date.
     *
     * @param date Assumes that the is formatted according to {@link VertretungsplanContract}
     * @return the row id of the date
     * @precondition The date is present in the database
     */
    private long getDateId(String date) {
        return addDate(date); // retrieves the row id of the date as the date has already been
        // inserted
    }

    @Override
    protected Void doInBackground(Void... params) {
        String[] vertretungenArray = null;
        VertretungsplanParser parser = new VertretungsplanParser();
        try {
            Document doc = parser.getDocumentViaLogin(VertretungsplanParser.URL_VERTRETUNGSPLAN);
            String[] dates = parser.getAvailableVertretungsplaeneDates(doc);
            HashMap<String, ArrayList<String[]>> vertretungsplanMap = parser.getVertretungsplan(doc);
            ArrayList<String[]> vertretungsplan = vertretungsplanMap.get(dates[0]);
            ArrayList<String> generalInfo = parser.getGeneralInfo(doc).get(dates[1]);
            ArrayList<String> absentClasses = parser.getAbsentClasses(doc).get(dates[0]);
            Log.v(LOG_TAG, "Connection established");
            DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMAN);
            if (dates != null) {
                mContext.getApplicationContext().getContentResolver().delete(Days.CONTENT_URI,
                        null, null);
                for (String date : dates) {
                    Date dateObj = dateFormat.parse(date, new ParsePosition(4));
                    addDate(VertretungsplanContract.convertDateToDatabaseFriendlyFormat(dateObj));
                }

            }
            if (vertretungsplan != null) {
                mContext.getApplicationContext().getContentResolver().delete(Vertretungen
                        .CONTENT_URI, null, null);
                for (String[] entry : vertretungsplan) {
                    addVertretungsplanEntry(entry, dates[0]);
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Async task failed.");
        }
        return null;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}

