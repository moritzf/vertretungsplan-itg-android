package de.itgdah.vertretungsplan.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import de.itgdah.vertretungsplan.data.VertretungsplanContract.AbsentClasses;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.Days;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.GeneralInfo;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.Vertretungen;

/**
 * Created by moritz on 23.03.15.
 */
public class VertretungsplanProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private VertretungsplanDbHelper mDbHelper;

    private static final int VERTRETUNGEN = 100;
    private static final int VERTRETUNGEN_WITH_DATE = 102;
    private static final int VERTRETUNGEN_WITH_DATE_AND_ID = 103;
    private static final int DAYS = 200;
    private static final int GENERAL_INFO = 300;
    private static final int ABSENT_CLASSES = 400;

    private static final SQLiteQueryBuilder sVertretungenByDateQueryBuilder;

    static {
        sVertretungenByDateQueryBuilder = new SQLiteQueryBuilder();
        sVertretungenByDateQueryBuilder.setTables(Vertretungen.TABLE_NAME + " JOIN " +
                Days.TABLE_NAME + " ON" + Days._ID + " = " + Vertretungen._ID);
    }

    private static final String sVertretungenDateSelection = Days.TABLE_NAME + "." + Days.COLUMN_DATE + "= ? ";
    private static final String sVertretungenDateAndIdSelection = sVertretungenDateSelection + " " +
            "AND " + Vertretungen.TABLE_NAME + "." + Vertretungen._ID + " = ?"; // first part of
            // the selection is the same as sVertretungenDateSelection

    private Cursor getVertretungenByDate(Uri uri, String[] projection, String sortOrder) {
        String date = Vertretungen.getDateFromUri(uri);

        String[] selectionArgs = {date};
        String selection = sVertretungenDateSelection;
        return sVertretungenByDateQueryBuilder.query(mDbHelper.getReadableDatabase(), projection,
                selection, selectionArgs, null, null, sortOrder);
    }

    private Cursor getVertretungenByDateAndId(Uri uri, String[] projection, String sortOrder) {
        String date = Vertretungen.getDateFromUri(uri);
        String id = Vertretungen.getIdFromUri(uri);

        String[] selectionArgs = {date, id};
        String selection = sVertretungenDateAndIdSelection;
        // same tables as VertretungenByDate
        return sVertretungenByDateQueryBuilder.query(mDbHelper.getReadableDatabase(),
                projection, selection, selectionArgs, null, null, sortOrder);
    }


    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = VertretungsplanContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, VertretungsplanContract.PATH_ABSENT_CLASSES, ABSENT_CLASSES);
        matcher.addURI(authority, VertretungsplanContract.PATH_GENERAL_INFO, GENERAL_INFO);
        matcher.addURI(authority, VertretungsplanContract.PATH_DAYS, DAYS);
        matcher.addURI(authority, VertretungsplanContract.PATH_VERTRETUNGEN + "/#", VERTRETUNGEN_WITH_DATE);
        matcher.addURI(authority, VertretungsplanContract.PATH_VERTRETUNGEN, VERTRETUNGEN);
        matcher.addURI(authority, VertretungsplanContract.PATH_VERTRETUNGEN + "/#/#", VERTRETUNGEN_WITH_DATE_AND_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new VertretungsplanDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // switch on the given uri and determine what request it is. After that the database can be
        // queried accordingly
        Cursor returnCursor;
        switch (sUriMatcher.match(uri)) {
            case VERTRETUNGEN: {
                returnCursor = mDbHelper.getReadableDatabase().query(Vertretungen
                                .TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            }
            case VERTRETUNGEN_WITH_DATE: {
                returnCursor = getVertretungenByDate(uri, projection, sortOrder);
                break;
            }
            case VERTRETUNGEN_WITH_DATE_AND_ID: {
                returnCursor = getVertretungenByDateAndId(uri, projection, sortOrder);
                break;
            }
            case DAYS: {
                returnCursor = mDbHelper.getReadableDatabase().query(Days
                                .TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            }
            case GENERAL_INFO: {
                returnCursor = mDbHelper.getReadableDatabase().query(GeneralInfo.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case ABSENT_CLASSES: {
                returnCursor = mDbHelper.getReadableDatabase().query(AbsentClasses.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return returnCursor;
    }


    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case VERTRETUNGEN_WITH_DATE:
                return Vertretungen.CONTENT_TYPE;
            case VERTRETUNGEN:
                return Vertretungen.CONTENT_TYPE;
            case VERTRETUNGEN_WITH_DATE_AND_ID:
                return Vertretungen.CONTENT_ITEM_TYPE;
            case GENERAL_INFO:
                return VertretungsplanContract.GeneralInfo.CONTENT_TYPE;
            case DAYS:
                return Days.CONTENT_TYPE;
            case ABSENT_CLASSES:
                return VertretungsplanContract.AbsentClasses.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);

        Uri returnUri;

        switch (match) {
            case VERTRETUNGEN: {
                long _id = db.insert(Vertretungen.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = VertretungsplanContract.Vertretungen
                            .buildVertretungenUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case DAYS: {
                long _id = db.insert(Days.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = Days.buildDaysUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into" + uri);
                }
                break;
            }
            case ABSENT_CLASSES: {
                long _id = db.insert(VertretungsplanContract.AbsentClasses.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = VertretungsplanContract.AbsentClasses.buildAbsentClassesUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into" + uri);
                }
                break;
            }
            case GENERAL_INFO: {
                long _id = db.insert(VertretungsplanContract.GeneralInfo.TABLE_NAME,
                        null, values);
                if (_id > 0) {
                    returnUri = VertretungsplanContract.GeneralInfo
                            .buildGeneralInfoUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into" + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case VERTRETUNGEN:
                rowsDeleted = db.delete(Vertretungen.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case DAYS:
                rowsDeleted = db.delete(Days.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case ABSENT_CLASSES:
                rowsDeleted = db.delete(VertretungsplanContract.AbsentClasses
                        .TABLE_NAME, selection, selectionArgs);
                break;
            case GENERAL_INFO:
                rowsDeleted = db.delete(GeneralInfo.TABLE_NAME, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case VERTRETUNGEN:
                rowsUpdated = db.update(Vertretungen.TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            case DAYS:
                rowsUpdated = db.update(Days.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case ABSENT_CLASSES:
                rowsUpdated = db.update(AbsentClasses.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case GENERAL_INFO:
                rowsUpdated = db.update(GeneralInfo.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " +
                        uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
