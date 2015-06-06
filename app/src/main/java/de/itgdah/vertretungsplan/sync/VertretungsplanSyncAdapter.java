package de.itgdah.vertretungsplan.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import de.itgdah.vertretungsplan.R;
import de.itgdah.vertretungsplan.data.VertretungsplanContract;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.AbsentClasses;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.Days;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.GeneralInfo;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.Vertretungen;
import de.itgdah.vertretungsplan.web.VertretungsplanParser;

/**
 * Used for syncing the vertretungsplan on the school server with the one on the device. Also
 * handles notifications.
 */
public class VertretungsplanSyncAdapter extends AbstractThreadedSyncAdapter {

    private final String LOG_TAG = VertretungsplanSyncAdapter.class.getSimpleName();

    // Interval at which to sync with the server.
    private static final int SYNC_INTERVAL = 60 * 360;
    // Used to save battery.
    private static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    public static final String SYNC_FINISHED = "finished";

    public VertretungsplanSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        updateDatabase();
        Intent i = new Intent(SYNC_FINISHED);
        getContext().sendBroadcast(i); // notify listeners that sync was performed.
    }

    private void updateDatabase() {

        VertretungsplanParser parser = new VertretungsplanParser(getContext());
        if (isOnline() && hasVertretungsplanChanged(parser.getDateStamp()
         )) {

            try {
                Document doc = parser.getDocumentViaLogin(VertretungsplanParser
                        .URL_VERTRETUNGSPLAN);

                String[] dates = parser.getAvailableVertretungsplaeneDates(doc);

                addDates(dates);
                addVertretungsplanEntries(parser.getVertretungsplan(doc), dates);
                addGeneralInfoEntries(parser.getGeneralInfo(doc), dates);
                addAbsentClassesEntries(parser.getAbsentClasses(doc), dates);


            } catch (Exception e) {
                Log.e(LOG_TAG, "Error retrieving vertretungsplan from server.");
            }
        }
    }

    /**
     * Checks if internet access is available.
     */
    private boolean isOnline() {
            ConnectivityManager cm =
            (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Inserts a new date into the days database table.
     *
     * @param date format: yyyyMMdd
     * @return the row ID of the added date
     */
    private long addDate(String date) {
            Cursor c = getContext().getContentResolver().query(Days.CONTENT_URI, new String[]
                    {Days._ID}, Days.COLUMN_DATE + " = ?", new String[] {date}, null);
            if(c.moveToFirst()) {
                long id = c.getLong(c.getColumnIndex(Days._ID));
                c.close();
                return id;
            } else {
                c.close();
                ContentValues dayValue = new ContentValues();
                dayValue.put(Days.COLUMN_DATE, date);

                Uri dateInsertUri = getContext().getContentResolver().insert(Days
                        .CONTENT_URI, dayValue);

                return ContentUris.parseId(dateInsertUri);
            }
    }

    private void addDates(String[] dates) {
        getContext().getApplicationContext().getContentResolver().delete(Days.CONTENT_URI,
                null, null);
        for (String date : dates) {
            addDate(date);
        }
    }

    /**
     * Inserts  new entries into the vertretungsplan database table. Assumes that the associated
     * dates of the entries, e.g. 20151105, are already present in the database.
     * @return number of rows added
     */
    private void addVertretungsplanEntries(HashMap<String, ArrayList<String[]>> vertretungsplanMap,
                                           String[] dates) {
        getContext().getApplicationContext().getContentResolver().delete(Vertretungen
                .CONTENT_URI, null, null);
        Vector<ContentValues> cvVector = new Vector<>();
        for (int i = 0; i < dates.length; i++) {
            ArrayList<String[]> vertretungsplan = vertretungsplanMap.get(dates[i]);
            if (vertretungsplan != null) {
                for (String[] entry : vertretungsplan) {
                   cvVector.add(getVertretungsplanContentValues(entry, dates[i]));
                }
            }
        }
        ContentValues[] cvArray = new ContentValues[cvVector.size()];
        cvVector.toArray(cvArray);
        getContext().getContentResolver().bulkInsert(Vertretungen
                .CONTENT_URI, cvArray);
    }

    private ContentValues getVertretungsplanContentValues(String[] entry, String date) {
        long dateId = getDateId(date);

        /** Look at {@link VertretungsplanParser} for more info on the
         * mapping. */
        final int PERIOD_INDEX = 0;
        final int CLASS_INDEX = 1;
        final int SUBJECT_INDEX = 2;
        final int VERTRETEN_DURCH_INDEX = 3;
        final int ROOM_INDEX = 4;
        final int COMMENT_INDEX = 5;

        String entryLowercase = entry[VERTRETEN_DURCH_INDEX].toLowerCase();
        if(entryLowercase.contains("eigenverantw.") || entryLowercase
                .contains("ur") || entryLowercase.contains("eva")) {
            entry[COMMENT_INDEX] = entry[VERTRETEN_DURCH_INDEX]; // swap
            // because the comment field is more suitable than the
            // vertreten_durch field in this case.
        } else {
            if(entry[COMMENT_INDEX].isEmpty()) {
                entry[COMMENT_INDEX] = "Vertretung"; // if the lesson is given by
                // another teacher the comment field is empty and only the
                // vertreten_durch field is populated. As we still want the
                // comment field to display a value we set it manually.
            }
        }

        ContentValues entryValues = new ContentValues();
        entryValues.put(Vertretungen.COLUMN_DAYS_KEY, dateId);
        entryValues.put(Vertretungen.COLUMN_PERIOD, entry[PERIOD_INDEX]);
        entryValues.put(Vertretungen.COLUMN_CLASS, entry[CLASS_INDEX]);
        entryValues.put(Vertretungen.COLUMN_SUBJECT, entry[SUBJECT_INDEX]);
        entryValues.put(Vertretungen.COLUMN_VERTRETEN_DURCH, entry[VERTRETEN_DURCH_INDEX]);
        entryValues.put(Vertretungen.COLUMN_ROOM, entry[ROOM_INDEX]);
        entryValues.put(Vertretungen.COLUMN_COMMENT, entry[COMMENT_INDEX]);

        return entryValues;
    }

    /**
     * Adds a general info line to the database.
     *
     * @param generalInfo String containing one line of the general information.
     * @param date        the date associated with the general info.
     */
    private void addGeneralInfoEntry(String generalInfo, String date) {
            ContentValues entryValues = new ContentValues();
            entryValues.put(GeneralInfo.COLUMN_MESSAGE, generalInfo.trim());
            entryValues.put(GeneralInfo.COLUMN_DAYS_KEY, getDateId(date));
            Uri generalInfoInsertUri = getContext().getContentResolver().insert(GeneralInfo
                    .CONTENT_URI, entryValues);
            ContentUris.parseId(generalInfoInsertUri);
    }

    private void addGeneralInfoEntries(HashMap<String, ArrayList<String>> generalInfoEntries,
                                       String[] dates) {
        getContext().getApplicationContext().getContentResolver().delete(GeneralInfo
                .CONTENT_URI, null, null);
        for (String date : dates) {
            ArrayList<String> generalInfo = generalInfoEntries.get(date);
            if (generalInfo != null) {
                for (String entry : generalInfo) {
                    addGeneralInfoEntry(entry, date);
                }
            }
        }
    }

    /**
     * Adds a general info line to the database.
     *
     * @param absentClass String array containing one absent class.
     * @param date        the date associated with the general info.
     */
    private void addAbsentClassesEntry(String[] absentClass, String date) {
            ContentValues entryValues = new ContentValues();
            // for an index mapping, please refer to the VertretungplanParser
            // class
            entryValues.put(AbsentClasses.COLUMN_CLASS, absentClass[0]);
            entryValues.put(AbsentClasses.COLUMN_PERIOD_RANGE, absentClass[1]);
            entryValues.put(AbsentClasses.COLUMN_COMMENT, absentClass[2]);
            entryValues.put(AbsentClasses.COLUMN_DAYS_KEY, getDateId(date));
            Uri absentClassesInsertUri = getContext().getContentResolver().insert(AbsentClasses
                    .CONTENT_URI, entryValues);
            ContentUris.parseId(absentClassesInsertUri);
    }

    private void addAbsentClassesEntries(HashMap<String, ArrayList<String[]>>
            absentClassesEntries,
                                         String[] dates) {
        getContext().getApplicationContext().getContentResolver().delete(AbsentClasses
                .CONTENT_URI, null, null);
        for (String date : dates) {
            ArrayList<String[]> absentClasses = absentClassesEntries.get(date);
            if (absentClasses != null) {
                for (String[] entry : absentClasses) {
                    addAbsentClassesEntry(entry, date);
                }
            }
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
    private static Account getSyncAccount(Context context) {
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
    private static void configurePeriodicSync(Context context) {
        Account account = getSyncAccount(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(VertretungsplanSyncAdapter.SYNC_INTERVAL, VertretungsplanSyncAdapter.SYNC_FLEXTIME).
                    setSyncAdapter(account, VertretungsplanContract.CONTENT_AUTHORITY).setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    VertretungsplanContract.CONTENT_AUTHORITY, new Bundle(), VertretungsplanSyncAdapter.SYNC_INTERVAL);
        }
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        VertretungsplanSyncAdapter.configurePeriodicSync(context);

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

    /**
     * If the vertretungsplan version on the device is the same as one on the server, there's no
     * point in updating the database.
     * @return true if the date stamp on the device and the one on the server don't match. false
     * otherwise.
     */
    private boolean hasVertretungsplanChanged(String dateStampOnServer) {
        // retrieve date stamp on device
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String dateStampKey = getContext().getString(R.string.date_stamp_key);
        String dateStampOnDevice = preferences.getString(dateStampKey, ""); // "" is the default
        // value if no value is associated with the key

        boolean result = (!dateStampOnDevice.equals(dateStampOnServer));

        if (result) {
            // store new date stamp on device
            preferences.edit().putString(dateStampKey, dateStampOnServer).apply();
        }

        return result;
    }

}
