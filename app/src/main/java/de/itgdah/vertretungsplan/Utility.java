package de.itgdah.vertretungsplan;

import java.text.DateFormat;
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

}
