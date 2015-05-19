package de.itgdah.vertretungsplan.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import org.jsoup.nodes.Document;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import de.itgdah.vertretungsplan.R;
import de.itgdah.vertretungsplan.data.VertretungsplanContract;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.AbsentClasses;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.Days;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.GeneralInfo;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.Vertretungen;
import de.itgdah.vertretungsplan.web.VertretungsplanParser;

/**
 * Created by Moritz on 5/18/2015.
 */
public class VertretungsplanSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = VertretungsplanSyncAdapter.class.getSimpleName();

    public static final int SYNC_INTERVAL = 60 * 360;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    public VertretungsplanSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
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

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences
                    (getContext());
            String key = getContext().getString(R.string.date_stamp_key);
            String savedDateStamp = preferences.getString(key,
                    "");
            String currentDateStamp = parser.getDateStamp();
            if((savedDateStamp.equals("")) || (!currentDateStamp.equals(savedDateStamp))) {
                preferences.edit().putString(key, currentDateStamp).apply();
                if (dates.length > 0) {
                    getContext().getApplicationContext().getContentResolver().delete(Days.CONTENT_URI,
                            null, null);
                    for (String date : dates) {
                        Date dateObj = dateFormat.parse(date, new ParsePosition(4));
                        addDate(VertretungsplanContract.convertDateToDatabaseFriendlyFormat(dateObj));
                    }
                }

                if (vertretungsplan != null) {
                    getContext().getApplicationContext().getContentResolver().delete(Vertretungen
                            .CONTENT_URI, null, null);
                    for(int i = 0; i < dates.length; i++) {
                        for (String[] entry : vertretungsplan) {
                            addVertretungsplanEntry(entry, dates[i]);
                        }
                    }
                }

                if (absentClasses != null) {
                    getContext().getApplicationContext().getContentResolver().delete(AbsentClasses
                            .CONTENT_URI, null, null);
                    for (int i = 0; i < dates.length; i++) {
                        for (String entry : absentClasses) {
                            addAbsentClassesEntry(entry, dates[i]);
                        }
                    }
                }

                if (generalInfo != null) {
                    getContext().getApplicationContext().getContentResolver().delete(GeneralInfo
                            .CONTENT_URI, null, null);
                    for (int i = 0; i < dates.length; i++) {
                        for (String entry : generalInfo) {
                            addGeneralInfoEntry(entry, dates[i]);
                        }
                    }
                }

            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "OnPerformSync failed.");
        }
        return;
    }


    /**
     * Inserts a new date into the days database table.
     *
     * @param date format: yyyyMMdd
     * @return the row ID of the added date
     */
    private long addDate(String date) {
        Cursor cursor = getContext().getContentResolver().query
                (Days.CONTENT_URI, new String[]{Days._ID},
                        Days.COLUMN_DATE + " = ?", new String[]{date},
                        null);
        if (cursor.moveToFirst()) {
            int dateIdIndex = cursor.getColumnIndex(Days._ID);
            return cursor.getLong(dateIdIndex);
        } else {
            ContentValues dayValue = new ContentValues();
            dayValue.put(Days.COLUMN_DATE, date);

            Uri dateInsertUri = getContext().getContentResolver().insert(Days
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
        Cursor cursor = getContext().getContentResolver().query(Vertretungen.CONTENT_URI, new
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

            Uri vertretungenInsertUri = getContext().getContentResolver().insert(Vertretungen
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
        Cursor cursor = getContext().getContentResolver().query(GeneralInfo.CONTENT_URI, new
                String[]{GeneralInfo._ID}, addEntrySelection, addEntrySelectionArgs, null);
        if (cursor.moveToFirst()) {
            int entryIdIndex = cursor.getColumnIndex(GeneralInfo._ID);
            return cursor.getLong(entryIdIndex);
        } else {
            ContentValues entryValues = new ContentValues();
            entryValues.put(GeneralInfo.COLUMN_MESSAGE, generalInfo);
            Uri generalInfoInsertUri = getContext().getContentResolver().insert(GeneralInfo
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
        Cursor cursor = getContext().getContentResolver().query(AbsentClasses.CONTENT_URI, new
                String[]{AbsentClasses._ID}, addEntrySelection, addEntrySelectionArgs, null);
        if (cursor.moveToFirst()) {
            int entryIdIndex = cursor.getColumnIndex(AbsentClasses._ID);
            return cursor.getLong(entryIdIndex);
        } else {
            ContentValues entryValues = new ContentValues();
            entryValues.put(AbsentClasses.COLUMN_MESSAGE, absentClass);
            Uri absentClassesInsertUri = getContext().getContentResolver().insert(AbsentClasses
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
    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                VertretungsplanContract.CONTENT_AUTHORITY, bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }


    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, VertretungsplanContract.CONTENT_AUTHORITY).setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    VertretungsplanContract.CONTENT_AUTHORITY, new Bundle(), syncInterval);
        }
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        VertretungsplanSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string
               .content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

}
