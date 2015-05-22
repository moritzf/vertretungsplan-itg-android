package de.itgdah.vertretungsplan;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.itgdah.vertretungsplan.data.VertretungsplanContract;

/**
 * Created by Moritz on 5/5/2015.
 */
public final class Utility {


    /**
     * Gets the day associated with the date. For example "Di" for 05.05.2015.
     * @param date the source date
     * @return a two-character day of the week string
     */
    public static String getDayOfTheWeekFromDate(Date date) {
        SimpleDateFormat dayFormatter = new SimpleDateFormat("EE", Locale.GERMAN);
        return dayFormatter.format(date);
    }

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
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(VertretungsplanContract.DATE_FORMAT, Locale.GERMAN);
        try {
            return dbDateFormat.parse(dateText);
        } catch ( ParseException e ) {
            e.printStackTrace();
            return null;
        }
    }
}
