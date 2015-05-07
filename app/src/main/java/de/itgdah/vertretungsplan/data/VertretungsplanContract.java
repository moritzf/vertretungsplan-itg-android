package de.itgdah.vertretungsplan.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by moritz on 23.03.15.
 */
public class VertretungsplanContract {

    // The "Content authority" is a name for the entire content provider.
    public static final String CONTENT_AUTHORITY = "de.itgdah.vertretungsplan";

    // Base of all URI's which are used to interact with the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Paths that are appended to the BASE_CONTENT_URI to provide access to
    // the individual tables
    public static final String PATH_DAYS = "days";
    public static final String PATH_VERTRETUNGEN = "vertretungen";
    public static final String PATH_ABSENT_CLASSES = "absent_classes";
    public static final String PATH_GENERAL_INFO = "general_info";
    public static final String PATH_PERSONAL_DATA = "personal_data";



    // Format used for storing dates in the database.  ALso used for converting those strings
    // back into date objects for comparison/processing.
    public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Converts the given date object to a database-friendly representation.
     * @param date the date to be converted
     *             preconditions: format: DateFormat.MEDIUM, Locale.GERMAN
     * @return date converted to the database format specified in {@link VertretungsplanContract}
     */
    public static String convertDateToDatabaseFriendlyFormat(Date date) {
        DateFormat dbFormatter = new SimpleDateFormat(VertretungsplanContract.DATE_FORMAT, Locale.GERMAN);
        return dbFormatter.format(date);
    }

    /**
     * Converts a dateText to a long Unix time representation
     * @param dateText the input date string
     * @return the Date object
     */
    public static Date getDateFromDb(String dateText) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.GERMAN);
        try {
            return dbDateFormat.parse(dateText);
        } catch ( ParseException e ) {
            e.printStackTrace();
            return null;
        }
    }

    /* Inner class that defines the table contents of the Vertretungen
    table. */
    public static final class Vertretungen implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_VERTRETUNGEN).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd." + CONTENT_AUTHORITY + "/" + 
                        PATH_VERTRETUNGEN;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd." + CONTENT_AUTHORITY + "/" + 
                        PATH_VERTRETUNGEN;

        // Table name
        public static final String TABLE_NAME = "vertretungen";

        // Period of the Vertretungen
        public static final String COLUMN_PERIOD = "period";

        // Class that is addressed by the Vertretungen
        public static final String COLUMN_CLASS = "class";

        // Subject that is addressed by the Vertretungen
        public static final String COLUMN_SUBJECT = "subject";

        // Defines the type of Vertretung. This can be either a person or
        // unsupervised work called EVA.
        public static final String COLUMN_VERTRETEN_DURCH = "vertreten_durch";

        // Room in which the Vertretungen takes place.
        public static final String COLUMN_ROOM = "room";

        // Comment that describes the Vertretung
        public static final String COLUMN_COMMENT = "comment";

        // Foreign key referencing the days table.
        public static final String COLUMN_DAYS_KEY = "days_id";

        public static Uri buildVertretungenUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getDateFromUri(Uri uri) {
            return uri.getPathSegments().get(1); //"authority/vertretungen/date",
        }

        // precondition: "uri of type "authority/vertretungen/date/id"
        public static String getIdFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }

    /* Inner class that defines the table contents of the general_info
    table. */
    public static final class GeneralInfo implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_GENERAL_INFO).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd." + CONTENT_AUTHORITY + "/" +
                        PATH_GENERAL_INFO;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd." + CONTENT_AUTHORITY + "/" +
                        PATH_GENERAL_INFO;
        
        // Table name
        public static final String TABLE_NAME = "general_info";
        
        // Message of the announcement.
        public static final String COLUMN_MESSAGE = "message";

        // Foreign key pointing to the days table.
        public static final String COLUMN_DAYS_KEY = "days_id";

        public static Uri buildGeneralInfoUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

    /* Inner class that defines the table contents of the days
    table. */
    public static final class Days implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DAYS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd." + CONTENT_AUTHORITY + "/" +
                        PATH_DAYS;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd." + CONTENT_AUTHORITY + "/" +
                        PATH_DAYS;
        
        // Table name
        public static final String TABLE_NAME = "days";

        // Date of the day
        public static final String COLUMN_DATE = "date";

        public static Uri buildDaysUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the table contents of the absent_classes
    table */
    public static final class AbsentClasses implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ABSENT_CLASSES).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd." + CONTENT_AUTHORITY + "/" +
                        PATH_ABSENT_CLASSES;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd." + CONTENT_AUTHORITY + "/" +
                        PATH_ABSENT_CLASSES;
        
        // Table name
        public static final String TABLE_NAME = "absent_classes";

        // Message defining the absent class and the reason why it is absent.
        public static final String COLUMN_MESSAGE = "message";

        // Foreign key pointing to the days table.
        public static final String COLUMN_DAYS_KEY = "days_id";

        public static Uri buildAbsentClassesUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the table contents of the personal data table. It's used to save
    the user subjects and the class of the user. This information is in turn used to select the
    entries of the personal vertretungsplan.
     */
    public static final class PersonalData implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath
                (PATH_PERSONAL_DATA).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd." + CONTENT_AUTHORITY + "/" +
                        PATH_PERSONAL_DATA;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd." + CONTENT_AUTHORITY + "/" +
                        PATH_PERSONAL_DATA;

        public static final String TABLE_NAME = "personal_data";

        // The class that the user is in.
        public static final String COLUMN_CLASS = "class";

        // The subject that the user wants to have displayed in the personal vertretungsplan.
        public static final String COLUMN_SUBJECT = "subject";

    }
}
