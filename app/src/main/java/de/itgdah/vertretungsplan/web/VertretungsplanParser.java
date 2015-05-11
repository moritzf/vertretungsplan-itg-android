package de.itgdah.vertretungsplan.web;

import android.util.Base64;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VertretungsplanParser implements LoginConstants {

    /** Constants */

    /** Used in the login method. */
    private static final String LOGIN = USERNAME + ":" + PASSWORD;
    private static final String BASE_64_LOGIN = new String(Base64.encodeToString(LOGIN.getBytes(),Base64.DEFAULT));

    /**
     * Url of the server directory containing the date stamp of the
     * Vertretungsplan.
     */
    public static final String URL_VERTRETUNGSPLAN_DIRECTORY =
            "http://www.itgdah.de/vp_app/";
    public static final String URL_VERTRETUNGSPLAN =
            "http://www.itgdah.de/vp_app/VertretungsplanApp.html";

    /**
     * Returns the document referenced by the url by performing a htaccess login.
     *
     * @throws MalformedURLException
     *
     * @precondition Url points to a website with the domain
     *               http://www.itgdah.de/vp_app/*
     */
    public Document getDocumentViaLogin(String url) throws MalformedURLException {
        try {
            return Jsoup.connect(url)
                    .header("Authorization", "Basic " + BASE_64_LOGIN).get();
        } catch (IOException e) {
            throw new MalformedURLException("The url doesn't exist");
        }
    }

    /** Gets the date stamp of the Vertretungsplan. */
    public String getDateStamp() {
        Document doc = null;
        try {
            doc = getDocumentViaLogin(URL_VERTRETUNGSPLAN_DIRECTORY);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        // Select row that contains the date string.
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
        // All h2 headings contain a Vertretungsplan date string.
        Elements dates = doc.select("h2");
        // date format 20.3.2015
        Pattern pattern = Pattern.compile("\\w+,.\\d+.\\d+.\\d+");
        Matcher matcher = pattern.matcher(dates.text());
        String[] dateArray = new String[3];
        int countDateArray = 0;
        while (matcher.find()) {
            dateArray[countDateArray++] = matcher.group();
        }
        return dateArray;
    }

    /**
     * Returns a HashMap of the absent classes where the key is the date and the
     * value is an array of the absent classes.
     */
    public HashMap<String, ArrayList<String>> getAbsentClasses(Document doc) {
        String[] dateArray = getAvailableVertretungsplaeneDates(doc);
        HashMap<String, ArrayList<String>> map =
                new HashMap<String, ArrayList<String>>();

        // all absent classes tables are of class K
        Elements absentClasses = doc.select("table.K");
        for (int i = 0; i < absentClasses.size(); i++) {
            ArrayList<String> list = new ArrayList<>();
            Element table = absentClasses.get(i);
            Elements rows = table.select("tr.K");
            for (Element row : rows) {
                list.add(row.text());
            }
            map.put(dateArray[i], list);
        }
        return map;
    }

    /**
     * Returns a HashMap of the general info for each day. The key is the date of
     * the day and the value is an ArrayList of the individual items of the
     * general info of the given day.
     */
    public HashMap<String, ArrayList<String>> getGeneralInfo(Document doc) {
        String[] dateArray = getAvailableVertretungsplaeneDates(doc);
        HashMap<String, ArrayList<String>> map =
                new HashMap<String, ArrayList<String>>();

        // all general info tables are of class F
        Elements generalInfo = doc.select("table.F");
        for (int i = 0; i < generalInfo.size(); i++) {
            Elements rows = generalInfo.get(i).select("tr.F");
            ArrayList<String> list = new ArrayList<>();
            String item = "";
            int numEmptyRows = 0;
            for (Element row : rows) {
                /**
                 * The following logic is required due to special formatting in the
                 * general info table.
                 *
                 */
                if (row.text().isEmpty()) {
                    numEmptyRows++;
                }

                if (!item.isEmpty()) {
                    item += "\n";
                }
                item += row.text();
                if ((row.text().isEmpty() && !item.isEmpty())
                        || ((numEmptyRows == 0) && !item.isEmpty())) {
                    list.add(item);
                    item = "";
                }
            }
            map.put(dateArray[i], list);
        }
        return map;
    }

    /**
     * Returns a HashMap of the Vertretungsplan for each day. The key is the date
     * of the day and the value is an ArrayList of the individual items of the
     * Vertretungsplan. The items are once again separated into 6 fields: period,
     * class, subject, vertreten_durch, room and comment.
     *
     */
    public HashMap<String, ArrayList<String[]>> getVertretungsplan(Document doc) {

        String[] dateArray = getAvailableVertretungsplaeneDates(doc);
        HashMap<String, ArrayList<String[]>> map =
                new HashMap<String, ArrayList<String[]>>();

	/*
	 * All Vertretungsplan tables are preceded by an h4 heading and are of class
	 * s.
	 */
        Elements vertretungsplaene = doc.select("h4 + table.s");

        for (int i = 0; i < vertretungsplaene.size(); i++) {
            Element table = vertretungsplaene.get(i);
            Elements periods = table.select("tr.s");
            ArrayList<String[]> listVertretungen = new ArrayList<>();

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
                for (int j = 0; j < numberOfVertretungen; j++) {
                    if (j == 0) {
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
}

