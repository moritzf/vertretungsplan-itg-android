package de.itgdah.vertretungsplan.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.itgdah.vertretungsplan.data.VertretungsplanContract;

/**
 * Defines utility functions like date conversion functions.
 */
public final class Utility {


    /**
     * Gets the day associated with the date. For example "Di" for 05.05.2015.
     * @param date the source date
     * @return a two-character day of the week string
     */
    public static String getDayOfTheWeekFromDate(Date date) {
        SimpleDateFormat dayFormatter = new SimpleDateFormat("E, dd.MM",
                Locale.GERMAN);
        String result = dayFormatter.format(date);
        return result.substring(0, 2) + result.substring(3); // necessary to
        // avoid dot (.) in day abbreviation, e.g. FR, instead of FR.
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
