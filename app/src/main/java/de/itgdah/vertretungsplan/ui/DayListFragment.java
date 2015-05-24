package de.itgdah.vertretungsplan.ui;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;

import de.itgdah.vertretungsplan.R;
import de.itgdah.vertretungsplan.data.VertretungsplanContract;
import de.itgdah.vertretungsplan.sync.VertretungsplanSyncAdapter;

/**
 * Created by moritz on 23.05.15.
 */
public class DayListFragment extends ListFragment implements
        LoaderManager
                .LoaderCallbacks<Cursor> {

    private static String[] dateIdsArray = new String[3];
    private static final String LOG_TAG = DayListFragment.class.getSimpleName();
    SimpleCursorAdapter mAdapter;

    private DayListFragment handle = this;

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

    // required for syncFinishedReceiver
    private final BroadcastReceiver syncFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(LOG_TAG, "Sync performed.");
            Cursor c = getActivity().getContentResolver().query(VertretungsplanContract.Days
                            .CONTENT_URI, new
                            String[]{VertretungsplanContract.Days._ID}, null, null,
                    VertretungsplanContract.Days._ID + " ASC");
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                dateIdsArray[i] = c.getString(0);
            }
            c.close();
            getLoaderManager().restartLoader(0, null, handle);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        String[] mVertretungsplanListColumns = {
                VertretungsplanContract.Vertretungen.COLUMN_PERIOD,
                VertretungsplanContract.Vertretungen.COLUMN_CLASS,
                VertretungsplanContract.Vertretungen.COLUMN_SUBJECT,
                VertretungsplanContract.Vertretungen.COLUMN_COMMENT,
                VertretungsplanContract.Vertretungen._ID};

        int[] mVertretungsplanListItems = {
                R.id.text_view_period, R.id.text_view_class, R.id.text_view_subject,
                R.id.text_view_comment
        };

        mAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.vertretungen_listitem,
                null,
                mVertretungsplanListColumns, // column names
                mVertretungsplanListItems, // view ids
                0);
        setListAdapter(mAdapter);

        getLoaderManager().initLoader(0, null, this);
        return inflater.inflate(R.layout.vertretungsplan_fragment, container,
                false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        int pos = getArguments().getInt("pos");
        String dayId = dateIdsArray[0] != null ? dateIdsArray[pos] :
                "";
        String[] selectionArgs;
        String selection;
        if (!dayId.isEmpty()) {
            selectionArgs = new String[]{dayId}; // index of column date
            selection = VertretungsplanContract.Vertretungen.COLUMN_DAYS_KEY + " = ?";
        } else {
            selectionArgs = null;
            selection = null;
        }
        return new CursorLoader(getActivity(), VertretungsplanContract.Vertretungen
                .CONTENT_URI, null, selection, selectionArgs, VertretungsplanContract.Vertretungen
                .COLUMN_PERIOD + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

}
