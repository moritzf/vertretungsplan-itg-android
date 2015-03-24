package de.itgdah.vertretungsplan.data;

import android.provider.BaseColumns;

/**
 * Created by moritz on 23.03.15.
 */
public class VertretungsplanContract {

    /* Inner class that defines the table contents of the Vertretungen
    table. */
    public static final class Vertretungen implements BaseColumns {

        // Table name
        public static final String TABLE_NAME = "vertretungen";

        // Primary key
        public static final String COLUMN_VERTRETUNGEN_ROW_ID =
                "vertretungenen_row_id";

        // Foreign key pointing to the days table.
        public static final String COLUMN_DAY_ID = "day_id";

        // Period of the Vertretungen
        public static final String COLUMN_PERIOD = "period";

        // Class that is addressed by the Vertretungen
        public static final String COLUMN_CLASS = "class";

        // Subject that is addressed by the Vertretungen
        public static final String COLUMN_SUBJECT = "subject";


        // Defines the type of Vertretungen. This can be either a person or
        // unsupervised work called EVA.
        public static final String COLUMN_VERTRETEN_DURCH = "vertreten_durch";

        // Room in which the Vertretungen takes place.
        public static final String COLUMN_ROOM = "room";

        // Comment that describes the Vertretung
        public static final String COLUMN_COMMENT = "comment";

    }

    /* Inner class that defines the table contents of the general_info
    table. */
    public static final class GeneralInfo implements BaseColumns {

        // Table name
        public static final String TABLE_NAME = "general_info";

        // Primary key
        public static final String COLUMN_GENERAL_INFO_ROW_ID =
                "general_info_row_id";

        // Message of the announcement.
        public static final String COLUMN_MESSAGE = "message";

        // Foreign key pointing to the days table
        public static final String COLUMN_DAY_ID = "day_id";

    }

    /* Inner class that defines the table contents of the days
    table. */
    public static final class Days implements BaseColumns {

        // Table name
        public static final String TABLE_NAME = "days";

        // Primary key
        public static final String COLUMN_DAY_ID =
                "day_id";

        // date of the day
        public static final String COLUMN_DATE = "date";

        /* Time of the last update. Currently all days have the same 'last
         updated' value. This field is used to check whether or not the
         database needs to be updated. */
        public static String COLUMN_LAST_UPDATED = "last_updated";

    }

    /* Inner class that defines the table contents of the absent_classes
    table */
    public static final class AbsentClasses implements BaseColumns {

        // Table name
        public static final String TABLE_NAME = "absent_classes";

        // Primary key
        public static final String COLUMN_ABSENT_CLASS_ID =
                "absent_class_id";

        // Message defining the absent class and the reason why it is absent.
        public static final String COLUMN_MESSAGE = "message";

        // Foreign key pointing to the days table.
        public static final String COLUMN_DAY_ID = "day_id";


    }
}
