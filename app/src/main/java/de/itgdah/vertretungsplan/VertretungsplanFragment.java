package de.itgdah.vertretungsplan;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.Date;

import de.itgdah.vertretungsplan.MainActivity;
import de.itgdah.vertretungsplan.R;
import de.itgdah.vertretungsplan.data.VertretungsplanContract;
import de.itgdah.vertretungsplan.sync.VertretungsplanSyncAdapter;
import de.itgdah.vertretungsplan.web.FetchVertretungsplanTask;

/**
 * A placeholder fragment containing a simple view.
 */
public class VertretungsplanFragment extends Fragment implements LoaderManager
        .LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = VertretungsplanFragment.class.getSimpleName();
    private SimpleCursorAdapter mVertretungsplanAdapter;

    public VertretungsplanFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        VertretungsplanSyncAdapter.initializeSyncAdapter(activity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        Toolbar toolbar=(Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setTitle(getResources().getStringArray(R.array.drawer_titles)[0]);
        toolbar.setTitleTextColor(Color.WHITE);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("running", true);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState == null) {
            updateVertretungsplan();
        }
        super.onCreate(savedInstanceState);
        String[] mVertretungsplanListColumns = {
                VertretungsplanContract.Vertretungen.COLUMN_PERIOD,
                VertretungsplanContract.Vertretungen.COLUMN_CLASS,
                VertretungsplanContract.Vertretungen.COLUMN_SUBJECT,
                VertretungsplanContract.Vertretungen.COLUMN_COMMENT,
                VertretungsplanContract.Vertretungen._ID
        };

        int[] mVertretungsplanListItems = {
                R.id.textView, R.id.textView2, R.id.textView3,
                R.id.textView5
        };

        mVertretungsplanAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.main_fragment_vertretungsplan_listitem,
                null,
                mVertretungsplanListColumns, // column names
                mVertretungsplanListItems, // view ids
                0);

    }

    private void updateVertretungsplan() {
        VertretungsplanSyncAdapter.syncImmediately(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_fragment_vertretungsplan, container, false);
        ListView vertretungsplanView = (ListView) rootView.findViewById(R.id.vertretungsplan_listview);
        vertretungsplanView.setAdapter(mVertretungsplanAdapter);
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        ContentResolver resolver = getActivity().getContentResolver();
        Log.v(LOG_TAG, DatabaseUtils.dumpCursorToString(resolver.query(VertretungsplanContract
                .Days.CONTENT_URI, null, null, null, null)));
        String[] selectionArgs = new String[] {"1"};
        return new CursorLoader(getActivity(), VertretungsplanContract.Vertretungen
                .CONTENT_URI, null, VertretungsplanContract.Vertretungen.COLUMN_DAYS_KEY + " = ?" ,
               selectionArgs,
                VertretungsplanContract.Vertretungen
                .COLUMN_PERIOD + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mVertretungsplanAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mVertretungsplanAdapter.swapCursor(null);
    }
}
