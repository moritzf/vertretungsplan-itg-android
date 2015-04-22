package de.itgdah.vertretungsplan.sync;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by moritz on 24.03.15.
 */
public class FetchVertretungsplanTask extends AsyncTask<Void, Void, String[]> {
    private ArrayAdapter<String> arrayAdapter;

    public FetchVertretungsplanTask(ArrayAdapter<String> arrayAdapter) {
        this.arrayAdapter = arrayAdapter;
    }

    @Override
    protected String[] doInBackground(Void... params) {
        String[] vertretungenArray = null;
        VertretungsplanParser parser = new VertretungsplanParser();
        try {
            Document doc = parser.getDocumentViaLogin(VertretungsplanParser.URL_VERTRETUNGSPLAN);
            HashMap<String, ArrayList<String[]>> vertretungen = parser.getVertretungsplan(doc);
            Log.v("async", "connection established");
            if (vertretungen != null) {
                ArrayList<String[]> list = vertretungen.get(parser.getAvailableVertretungsplaeneDates(doc)[0]);
                vertretungenArray = new String[list.size()];
                int count = 0;
                for (String[] arr : list) {
                    vertretungenArray[count++] = Arrays.toString(arr);
                }
            }
        } catch (Exception e) {
            Log.e("Error", "error");
        }
        return vertretungenArray;
    }

    protected void onPostExecute(String[] result) {
        if (result != null) {
            for (String item : result) {
                Log.e("Async",item);
                System.out.println(item);
                arrayAdapter.add(item);
            }
        }
    }
}

