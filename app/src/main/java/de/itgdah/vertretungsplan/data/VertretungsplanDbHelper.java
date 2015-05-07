package de.itgdah.vertretungsplan.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import de.itgdah.vertretungsplan.data.VertretungsplanContract.AbsentClasses;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.Days;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.GeneralInfo;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.Vertretungen;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.PersonalData;

/**
 * Manages a local database for the Vertretungsplan.
 */
public class VertretungsplanDbHelper extends SQLiteOpenHelper {

    /* Denotes the database version. Increment this value,
     if the database schema changes */
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "vertretungsplan.db";

    public VertretungsplanDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_DAYS_TABLE = "CREATE TABLE " +
                Days.TABLE_NAME + " (" +
                Days._ID + " INTEGER " + "PRIMARY KEY " +
                "AUTOINCREMENT," +
                Days.COLUMN_DATE + " TEXT" + ")";

        final String SQL_CREATE_ABSENT_CLASSES_TABLE = "CREATE TABLE " +
                AbsentClasses.TABLE_NAME + " (" +
                AbsentClasses._ID + " INTEGER PRIMARY KEY " +
                "AUTOINCREMENT," +
                AbsentClasses.COLUMN_DAYS_KEY + " INTEGER, " +
                AbsentClasses.COLUMN_MESSAGE + " TEXT," +
                " FOREIGN KEY (" + AbsentClasses.COLUMN_DAYS_KEY + ") " +
                "REFERENCES " + Days.TABLE_NAME + " (" + Days._ID +
                ") )";

        final String SQL_CREATE_GENERAL_INFO_TABLE = "CREATE TABLE " +
                GeneralInfo.TABLE_NAME + " (" +
                GeneralInfo._ID + " INTEGER PRIMARY " +
                "KEY AUTOINCREMENT," +
                GeneralInfo.COLUMN_DAYS_KEY + " INTEGER, " +
                GeneralInfo.COLUMN_MESSAGE + " TEXT, " +
                "FOREIGN KEY (" + GeneralInfo.COLUMN_DAYS_KEY + ") " +
                "REFERENCES " + Days.TABLE_NAME + " (" + Days._ID +
                ") )";



        final String SQL_CREATE_VERTRETUNGEN_TABLE = "CREATE TABLE " +
                Vertretungen.TABLE_NAME + " (" +
                Vertretungen._ID + " INTEGER PRIMARY " +
                "KEY AUTOINCREMENT," +
                Vertretungen.COLUMN_DAYS_KEY + " INTEGER, " +
                Vertretungen.COLUMN_PERIOD + " TEXT," +
                Vertretungen.COLUMN_CLASS + " TEXT," +
                Vertretungen.COLUMN_SUBJECT + " TEXT," +
                Vertretungen.COLUMN_VERTRETEN_DURCH + " TEXT," +
                Vertretungen.COLUMN_ROOM + " TEXT," +
                Vertretungen.COLUMN_COMMENT + " TEXT, " +
                "FOREIGN KEY (" + Vertretungen.COLUMN_DAYS_KEY + ") " +
                "REFERENCES " +
                Days.TABLE_NAME + " (" + Days._ID + ") )";
                /* Because the fields are used as TEXT in the app,
                they are saved as TEXT form in the database as well,
                even though another data type might be more appropriate */

        final String SQL_CREATE_PERSONAL_DATA_TABLE = "CREATE TABLE " +
                PersonalData.TABLE_NAME + " (" +
                PersonalData._ID + " INTEGER PRIMARY " + "KEY AUTOINCREMENT," +
                PersonalData.COLUMN_CLASS + " TEXT" +
                PersonalData.COLUMN_SUBJECT + " TEXT" + " )";



        db.execSQL(SQL_CREATE_DAYS_TABLE);
        db.execSQL(SQL_CREATE_ABSENT_CLASSES_TABLE);
        db.execSQL(SQL_CREATE_GENERAL_INFO_TABLE);
        db.execSQL(SQL_CREATE_VERTRETUNGEN_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Only fires when database schema version changes
        db.execSQL("DROP TABLE IF EXISTS " + Days.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AbsentClasses.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + GeneralInfo.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Vertretungen.TABLE_NAME);
        onCreate(db);
    }
}
