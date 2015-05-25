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
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;

import com.commonsware.cwac.merge.MergeAdapter;

import de.itgdah.vertretungsplan.R;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.Vertretungen;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.GeneralInfo;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.Days;
import de.itgdah.vertretungsplan.sync.VertretungsplanSyncAdapter;

/**
 * Represents one day of the vertretungsplan containing the vertretungen and
 * the general info of the day. This class is used in the view pager and it's
 * the content frame of the selected day.
 */
public class DayListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /* Used in DaysPagerAdapter and denotes the position of this fragment in
    the view pager
     */
    public static final String PAGER_POSITION_KEY = "pos";

    private static final String LOG_TAG = DayListFragment.class.getSimpleName();
    private static final int VERTRETUNGEN_LOADER_ID = 0;
    private static final int GENERAL_INFO_LOADER_ID = 1;
    /**
     * Stores the dateIds of the days in the vertretungsplan table. Used for
     * filtering by date in onCreateLoader.
     */
    private static String[] dateIdsArray = new String[3];
    SimpleCursorAdapter mVertretungenAdapter;
    SimpleCursorAdapter mGeneralInfoAdapter;
    MergeAdapter mMergeAdapter;

    /**
     * Used as a workaround for the fact that the loader may be created
     * before the database has been fully build.
     */
    private boolean mNullFix;

    // necessary for restartLoader in onReceive.
    private DayListFragment handle = this;
    /**
     * Called when onPerformSync in {@link VertretungsplanSyncAdapter} has
     * finished.
     */
    private final BroadcastReceiver syncFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(LOG_TAG, "Sync performed.");

/*             populates the dateIdsArray. This logic is necessary as we
             can't depend on the date table being already populated in
             onCreateLoader. As the date ids are necessary for filtering we
             fetch them after the sync with the server has been completed,
             i.e. onPerformSync has finished.*/
            Cursor c = getActivity().getContentResolver().query(Days
                            .CONTENT_URI, new
                            String[]{Days._ID}, null, null,
                    Days._ID + " ASC");
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                dateIdsArray[i] = c.getString(0);
            }
            c.close();


            getLoaderManager().restartLoader(VERTRETUNGEN_LOADER_ID, null, handle);
            getLoaderManager().restartLoader(GENERAL_INFO_LOADER_ID, null,
                    handle);
        }
    };

    private View mVertretungenHeader;
    private View mGeneralInfoHeader;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout
                .vertretungsplan_fragment, container, false);

        // mVertretungsplanAdapter init
        String[] mVertretungsplanListColumns = {
                Vertretungen.COLUMN_PERIOD,
                Vertretungen.COLUMN_CLASS,
                Vertretungen.COLUMN_SUBJECT,
                Vertretungen.COLUMN_COMMENT,
                Vertretungen.COLUMN_VERTRETEN_DURCH,
                Vertretungen._ID};

        int[] mVertretungsplanListItems = {
                R.id.text_view_period, R.id.text_view_class, R.id.text_view_subject,
                R.id.text_view_comment
        };

        mVertretungenAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.vertretungen_listitem,
                null,
                mVertretungsplanListColumns, // column names
                mVertretungsplanListItems, // view ids
                0);

        // mGeneralInfoAdapter init
        String[] mGeneralInfoListColumns = {
                GeneralInfo.COLUMN_MESSAGE,
                GeneralInfo._ID,
        };
        int[] mGeneralInfoListItems = {
                R.id.text_view_general_info };
        mGeneralInfoAdapter = new SimpleCursorAdapter(getActivity(), R.layout
                .general_info_item, null, mGeneralInfoListColumns,
                mGeneralInfoListItems, 0);

        // list view headers init
        LayoutInflater factory = LayoutInflater.from(getActivity());
        mVertretungenHeader = factory.inflate(R.layout
                .vertretungen_header, null);
        mGeneralInfoHeader = factory.inflate(R.layout
                .general_info_header, null);

        // mMergeAdapter init
        mMergeAdapter = new MergeAdapter();
        mMergeAdapter.addView(mVertretungenHeader);
        mMergeAdapter.addAdapter(mVertretungenAdapter);
        mMergeAdapter.addView(mGeneralInfoHeader);
        mMergeAdapter.addAdapter(mGeneralInfoAdapter);
        setListAdapter(mMergeAdapter);
        mMergeAdapter.setActive(mGeneralInfoHeader, false);

        getLoaderManager().initLoader(GENERAL_INFO_LOADER_ID, null, this);
        getLoaderManager().initLoader(VERTRETUNGEN_LOADER_ID, null, this);
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        int position = getArguments().getInt(PAGER_POSITION_KEY);
        // check if dates are available
        String dayId = dateIdsArray[0] != null ? dateIdsArray[position] : "";
        String[] selectionArgs;
        String selection;

        if (!dayId.isEmpty()) {
            // if the dayIds are available, select date id that corresponds
            // to the position in the pager.
            selectionArgs = new String[]{dayId};
            selection = Vertretungen.COLUMN_DAYS_KEY + " = ?";
            mNullFix = false;
        } else {
            // return all days
            selectionArgs = null;
            selection = null;
            mNullFix = true;
        }
        switch (id) {
            case VERTRETUNGEN_LOADER_ID: {
                return new CursorLoader(getActivity(), Vertretungen
                        .CONTENT_URI, null, selection, selectionArgs,
                        null);
            }
            case GENERAL_INFO_LOADER_ID: {
                return new CursorLoader(getActivity(),
                        GeneralInfo.CONTENT_URI,
                        null, selection, selectionArgs,
                        GeneralInfo._ID + " ASC");
            }
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        switch (id) {
            case VERTRETUNGEN_LOADER_ID: {
                mVertretungenAdapter.swapCursor
                        (data);
            }
            break;
            case GENERAL_INFO_LOADER_ID: {
                if (!mNullFix && data.getCount() > 0) {
                    mMergeAdapter.setActive(mGeneralInfoHeader, true);
                }
                mGeneralInfoAdapter.swapCursor(data);
            }
            break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        int id = loader.getId();
        switch (id) {
            case VERTRETUNGEN_LOADER_ID: {
                mVertretungenAdapter.swapCursor(null);
            }
            break;
            case GENERAL_INFO_LOADER_ID: {
                mGeneralInfoAdapter.swapCursor(null);
            }
            break;
        }
    }

}
