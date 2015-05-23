package de.itgdah.vertretungsplan;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import de.itgdah.vertretungsplan.data.VertretungsplanContract;
import de.itgdah.vertretungsplan.sync.VertretungsplanSyncAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class VertretungsplanFragment extends Fragment implements LoaderManager
        .LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = VertretungsplanFragment.class.getSimpleName();
    private SimpleCursorAdapter mVertretungsplanAdapter;

    // required for syncFinishedReceiver
    VertretungsplanFragment handle = this;
    private BroadcastReceiver syncFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "SYNC!");
            getLoaderManager().restartLoader(0, null, handle);
        }
    };


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
                R.id.text_view_period, R.id.text_view_class, R.id.text_view_subject,
                R.id.text_view_comment
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
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(syncFinishedReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(syncFinishedReceiver, new IntentFilter
                (VertretungsplanSyncAdapter.SYNC_FINISHED));
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
        Cursor c = getActivity().getContentResolver().query(VertretungsplanContract.Days
                .CONTENT_URI, new String[] {"MIN(" + VertretungsplanContract.Days._ID + ")"}, null
                , null , null);
        if(c.moveToFirst()) {}
        String dayId = c.getString(0);
        String[] selectionArgs;
        String selection;
        if (dayId != null) {
            selectionArgs = new String[] {c.getString(0)}; // index of column date
            selection = VertretungsplanContract.Vertretungen.COLUMN_DAYS_KEY + " = ?";
        } else {
            selectionArgs = null;
            selection = null;
        }
        c.close();
        return new CursorLoader(getActivity(), VertretungsplanContract.Vertretungen
                .CONTENT_URI, null, selection,selectionArgs, VertretungsplanContract.Vertretungen
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
