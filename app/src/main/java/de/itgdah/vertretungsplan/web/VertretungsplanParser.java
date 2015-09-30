package de.itgdah.vertretungsplan.web;

import android.content.Context;
import android.util.Base64;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.itgdah.vertretungsplan.util.Utility;

public class VertretungsplanParser implements LoginConstants {

    Context mContext;

    public VertretungsplanParser(Context context) {
        mContext = context;
    }

    /** Constants */

    /**
     * Used in the login method.
     */
    private static final String LOGIN = USERNAME + ":" + PASSWORD;
    private static final String BASE_64_LOGIN = Base64.encodeToString(LOGIN.getBytes(), Base64.DEFAULT);
    private String[] dateArray = new String[3];
    private boolean isDateArraySet = false;

    /**
     * Url of the server directory containing the date stamp of the
     * Vertretungsplan.
     */
    private static final String URL_VERTRETUNGSPLAN_DIRECTORY =
            "http://www.itgdah.de/vp_app/";
    public static final String URL_VERTRETUNGSPLAN =
            "http://www.itgdah.de/vp_app/VertretungsplanApp.html";

    public static final int CONNECTION_TIMEOUT = 500; // in milliseconds

    /**
     * Returns the document referenced by the url by performing a htaccess login.
     *
     * @throws MalformedURLException
     * @precondition Url points to a website with the domain
     * http://www.itgdah.de/vp_app/*
     */
    public Document getDocumentViaLogin(String url) throws MalformedURLException {
        try {
            return Jsoup.connect(url).timeout(CONNECTION_TIMEOUT)
                    .header("Authorization", "Basic " + BASE_64_LOGIN).get();
        } catch (IOException e) {
            throw new MalformedURLException("The url doesn't exist");
        }
    }

    /**
     * Gets the date stamp of the Vertretungsplan. If the date stamp can't be retrieved, an exception is thrown.
     */
    public String getDateStamp() {
        Document doc = null;
        try {
            doc = getDocumentViaLogin(URL_VERTRETUNGSPLAN_DIRECTORY);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        // Select row that contains the date string.
        if (doc == null) {
            return "";
        }
        String data = doc.select("pre").text();
        // Date string format: 2015-03-20 11:37
        // Get date string via regular expression matching.
        Pattern pattern = Pattern.compile("\\d+-\\d+-\\d+.\\d+:\\d+");
        Matcher matcher = pattern.matcher(data);
        // necessary for the matcher to engage
        matcher.find();
        // only one date available, so the first match is returned
        return matcher.group(0);
    }

    /**
     * Gets the dates of the available Vertretungsplaene and returns an array of
     * them with the first element being the most immediate date. For Example
     * 13/03/15 comes before 14/03/15.
     */
    public String[] getAvailableVertretungsplaeneDates(Document doc) {
        if (isDateArraySet) {
            return dateArray;
        }
        // All h2 headings contain a Vertretungsplan date string.
        Elements dates = doc.select("h2");
        // date format 20.3.2015
        Pattern pattern = Pattern.compile("\\w+,.\\d+.\\d+.\\d+");
        Matcher matcher = pattern.matcher(dates.text());
        String[] dateArray = new String[3];
        int countDateArray = 0;
        DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMAN);
        while (matcher.find()) {
            Date dateObj = dateFormat.parse(matcher.group(), new ParsePosition(4));
            dateArray[countDateArray++] = Utility.convertDateToDatabaseFriendlyFormat(dateObj);
        }
        this.dateArray = dateArray;
        isDateArraySet = true;
        return dateArray;
    }

    private String[] getRawAvailableVertretungsplaeneDates(Document doc) {
        String[] dateArray = new String[3];
        Elements dates = doc.select("a[href]");

        for (int i = 0; i < dates.size(); i++) {
            dateArray[i] = dates.get(i).text();
        }
        return dateArray;
    }

    /**
     * Returns a HashMap of the absent classes where the key is the date and the
     * value is an array of the absent classes. Each absent class is defined
     * by an array consisting of the fields class, period range, comment in
     * this order.
     */
    public HashMap<String, ArrayList<String[]>> getAbsentClasses(Document doc) {
        String[] normalizedDateArray = getAvailableVertretungsplaeneDates(doc);
        String[] rawDateArray = getRawAvailableVertretungsplaeneDates(doc);
        HashMap<String, ArrayList<String[]>> map =
                new HashMap<>();

        for (int i = 0; i < rawDateArray.length; i++) {
            Elements succeedingElements = doc.select("a[name=" + rawDateArray[i] + "] ~ *");
            ArrayList<String[]> list = new ArrayList<>();
            for (int j = 0; j < succeedingElements.size(); j++) {
                if (succeedingElements.get(j).tagName().equals("table") && succeedingElements.get(j)
                        .className().equals("K")) {
                    Element absentClasses = succeedingElements.get(j);
                    Elements rows = absentClasses.select("tr.K");
                    for (Element row : rows) {
                        list.add(splitAbsentClassesRowIntoChunks(row));
                    }
                    map.put(normalizedDateArray[i], list);
                }
                if (succeedingElements.get(j).tagName().equals("hr")) break;
            }
            map.put(normalizedDateArray[i], list); // empty list since no absent classes for the
            // day were found.
        }

        return map;
    }

    /**
     * Returns a HashMap of the general info for each day. The key is the date of
     * the day and the value is an ArrayList of the individual items of the
     * general info of the given day.
     */
    public HashMap<String, ArrayList<String>> getGeneralInfo(Document doc) {
        String[] normalizedDateArray = getAvailableVertretungsplaeneDates(doc);
        String[] rawDateArray = getRawAvailableVertretungsplaeneDates(doc);
        HashMap<String, ArrayList<String>> map =
                new HashMap<>();

        for (int i = 0; i < rawDateArray.length; i++) {
            ArrayList<String> list = new ArrayList<>();
            Elements succeedingElements = doc.select("a[name=" + rawDateArray[i] + "] ~ *");
            for (int j = 0; j < succeedingElements.size(); j++) {
                if (succeedingElements.get(j).tagName().equals("table") && succeedingElements.get(j)
                        .className().equals("F")) {
                    Element generalInfo = succeedingElements.get(j);
                    Elements rows = generalInfo.select("th.F");
                    for (Element row : rows) {
                        if (!row.text().isEmpty())
                            list.add(row.text());
                    }
                    map.put(dateArray[i], list);
                }
                if (succeedingElements.get(j).tagName().equals("hr")) break;
            }
            map.put(normalizedDateArray[i], list); // empty list since no absent classes for the
            // day were found.
        }
        return map;
    }

    /**
     * Returns a HashMap of the Vertretungsplan for each day. The key is the date
     * of the day and the value is an ArrayList of the individual items of the
     * Vertretungsplan. The items are once again separated into 6 fields: period,
     * class, subject, vertreten_durch, room and comment.
     */
    public HashMap<String, ArrayList<String[]>> getVertretungsplan(Document doc) {

        String[] normalizedDateArray = getAvailableVertretungsplaeneDates(doc);
        String[] rawDateArray = getRawAvailableVertretungsplaeneDates(doc);
        HashMap<String, ArrayList<String[]>> map =
                new HashMap<>();

        for (int i = 0; i < rawDateArray.length; i++) {
            ArrayList<String[]> listVertretungen = new ArrayList<>();
            Elements succeedingElements = doc.select("a[name=" + rawDateArray[i] + "] ~ *");
            for (int j = 0; j < succeedingElements.size(); j++) {
                if (succeedingElements.get(j).tagName().equals("table") && succeedingElements.get(j)
                        .className().equals("s")) {
                    Element vertretungsplan = succeedingElements.get(j);
                    Elements periods = vertretungsplan.select("tr.s");
                    for (Element period : periods) {
                    /*
                     * Every period has a header with a rowspan attribute that specifies
                     * the number of Vertretungen of the period. For example, if a period
                     * has a header rowspan attribute with the value 7, that means, that
                     * there 7 Vertretungen for this period.
                     */
                        int numberOfVertretungen =
                                Integer.valueOf(period.select("th.s").attr("rowspan"));
                        Element next = null;
                        for (int k = 0; k < numberOfVertretungen; k++) {
                            if (k == 0) {
                                next = period;
                            } else {
                                next = next.nextElementSibling();
                            }
                            // The table header of class s contains the period number
                            String periodNumber = period.select("th.s").text();
                            String[] rowElems =
                                    splitVertretungsplanRowIntoChunks(next, periodNumber);
                            listVertretungen.add(rowElems);
                        }
                    }
                    map.put(dateArray[i], listVertretungen);
                }
                if (succeedingElements.get(j).tagName().equals("hr")) break;
            }
            map.put(normalizedDateArray[i], listVertretungen); // empty list since no absent classes for the
            // day were found.
        }

        return map;
    }

    /**
     * Splits the specified row into six tokens (period, class, subject,
     * vertreten_durch, room and comment) and returns them as a fixed-length
     * array.
     */
    private String[]
    splitVertretungsplanRowIntoChunks(Element row, String period) {

        Elements rawRowElems = row.select("td");
        String[] rowTokens = new String[6];
        int count = 0;
        for (int i = 0; i < rawRowElems.size(); i++) {
            if (i == 0) {
                rowTokens[count++] = period;
            }
            // Trim non-breaking spaces
            rowTokens[count++] = rawRowElems.get(i).text().replaceAll("\\u00A0", "");
        }
        return rowTokens;
    }

    /**
     * Splits the specified row into three tokens (class, periods, comment)
     * and returns them as a fixed-length array;
     */
    private String[] splitAbsentClassesRowIntoChunks(Element row) {
        String[] rowTokens = new String[3];
        rowTokens[0] = row.getElementsByTag("th").text();
        Elements rawRowElems = row.select("td");
        for (int i = 1; i <= rawRowElems.size(); i++) {
            rowTokens[i] = rawRowElems.get(i - 1).text();
        }
        return rowTokens;
    }
}

