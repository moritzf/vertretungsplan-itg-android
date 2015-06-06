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

import com.commonsware.cwac.merge.MergeAdapter;

import de.itgdah.vertretungsplan.R;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.AbsentClasses;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.GeneralInfo;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.Vertretungen;
import de.itgdah.vertretungsplan.sync.VertretungsplanSyncAdapter;

/**
 * Base fragment that contains common parts of the vertretungsplan list view,
 * i.e. vertretungen and general infos.
 */
public class BaseDayListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /* Used to as a filter to only show entries associated with the specified
     day id */
    public static final String DAY_ID_KEY = "day_id";

    private static final String LOG_TAG = GeneralDayListFragment.class.getSimpleName();

    /* Loader ids */
    private static final int VERTRETUNGEN_LOADER_ID = 0;
    private static final int GENERAL_INFO_LOADER_ID = 1;
    private static final int ABSENT_CLASSES_LOADER_ID = 2;

    SimpleCursorAdapter mVertretungenAdapter;
    SimpleCursorAdapter mGeneralInfoAdapter;
    SimpleCursorAdapter mAbsentClassesAdapter;
    MergeAdapter mMergeAdapter;

    // necessary for restartLoader in onReceive.
    private BaseDayListFragment mHandle = this;
    /**
     * Called when onPerformSync in {@link VertretungsplanSyncAdapter} has
     * finished.
     */
    private final BroadcastReceiver syncFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(LOG_TAG, "Sync performed.");

            getLoaderManager().restartLoader(VERTRETUNGEN_LOADER_ID, null, mHandle);
            getLoaderManager().restartLoader(GENERAL_INFO_LOADER_ID, null, mHandle);
            getLoaderManager().restartLoader(ABSENT_CLASSES_LOADER_ID, null, mHandle);

        }
    };

    private View mVertretungenHeader;
    private View mGeneralInfoHeader;
    private String mSelection = null;
    private View mAbsentClassesHeader;

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
        View rootView = inflater.inflate(R.layout
                .sliding_tabs_vertretungsplan_day_fragment, container, false);

        // mVertretungsplanAdapter init
        String[] vertretungsplanListColumns = {
                Vertretungen.COLUMN_PERIOD,
                Vertretungen.COLUMN_CLASS,
                Vertretungen.COLUMN_SUBJECT,
                Vertretungen.COLUMN_COMMENT,
                Vertretungen.COLUMN_VERTRETEN_DURCH,
                Vertretungen._ID};

        int[] vertretungsplanListItems = {
                R.id.text_view_vertretungen_period, R.id.text_view_vertretungen_class, R.id.text_view_vertretungen_subject,
                R.id.text_view_vertretungen_comment
        };

        mVertretungenAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.vertretungen_list_item,
                null,
                vertretungsplanListColumns, // column names
                vertretungsplanListItems, // view ids
                0);

        // mGeneralInfoAdapter init
        String[] generalInfoListColumns = {
                GeneralInfo.COLUMN_MESSAGE,
                GeneralInfo._ID,
        };
        int[] generalInfoListItems = {
                R.id.text_view_general_info};
        mGeneralInfoAdapter = new SimpleCursorAdapter(getActivity(), R.layout
                .general_info_list_item, null, generalInfoListColumns,
                generalInfoListItems, 0);

        // mAbsentClassesAdapter init
        String[] absentClassesListColumns = {
                AbsentClasses.COLUMN_PERIOD_RANGE,
                AbsentClasses.COLUMN_CLASS,
                AbsentClasses.COLUMN_COMMENT,
                AbsentClasses._ID
        };
        int[] absentClassesInfoListItems = {
                R.id.text_view_absent_classes_period_range,
                R.id.text_view_absent_classes_class,
                R.id.text_view_absent_classes_comment
        };
        mAbsentClassesAdapter = new SimpleCursorAdapter(getActivity(), R
                .layout.absent_classes_list_item, null,
                absentClassesListColumns, absentClassesInfoListItems, 0);

        // list view headers init
        LayoutInflater factory = LayoutInflater.from(getActivity());
        mVertretungenHeader = factory.inflate(R.layout
                .vertretungen_list_header, null);
        mGeneralInfoHeader = factory.inflate(R.layout
                .general_info_list_header, null);
        mAbsentClassesHeader = factory.inflate(R.layout
                .absent_classes_header, null);

        // mMergeAdapter init
        mMergeAdapter = new MergeAdapter();
        mMergeAdapter.addView(mVertretungenHeader);
        mMergeAdapter.setActive(mVertretungenHeader, false);
        mMergeAdapter.addAdapter(mVertretungenAdapter);
        mMergeAdapter.addView(mGeneralInfoHeader);
        mMergeAdapter.setActive(mGeneralInfoHeader, false);
        mMergeAdapter.addAdapter(mGeneralInfoAdapter);
        mMergeAdapter.addView(mAbsentClassesHeader);
        mMergeAdapter.setActive(mAbsentClassesHeader, false);
        mMergeAdapter.addAdapter(mAbsentClassesAdapter);
        setListAdapter(mMergeAdapter);

        getLoaderManager().initLoader(GENERAL_INFO_LOADER_ID, null, this);
        getLoaderManager().initLoader(VERTRETUNGEN_LOADER_ID, null, this);
        getLoaderManager().initLoader(ABSENT_CLASSES_LOADER_ID, null, this);
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String dayId = getArguments().getString(DAY_ID_KEY);
        String[] selectionArgs;
        if (dayId != null) {
            selectionArgs = new String[]{dayId};
        } else {
           selectionArgs = null;
        }
        mSelection = Vertretungen.COLUMN_DAYS_KEY + " = ?";

        switch (id) {
            case VERTRETUNGEN_LOADER_ID: {
                return new CursorLoader(getActivity(), Vertretungen
                        .CONTENT_URI, null, mSelection, selectionArgs,
                        null);
            }
            case GENERAL_INFO_LOADER_ID: {
                return new CursorLoader(getActivity(),
                        GeneralInfo.CONTENT_URI,
                        null, mSelection, selectionArgs,
                        GeneralInfo._ID + " ASC");
            }
            case ABSENT_CLASSES_LOADER_ID: {
                return new CursorLoader(getActivity(), AbsentClasses
                        .CONTENT_URI, null, mSelection, selectionArgs, null);
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
                if (data.getCount() > 0) {
                    mMergeAdapter.setActive(mVertretungenHeader, true);
                }
                mVertretungenAdapter.swapCursor
                        (data);
            }
            break;
            case GENERAL_INFO_LOADER_ID: {
                if (data.getCount() > 0) {
                    mMergeAdapter.setActive(mGeneralInfoHeader, true);
                }
                mGeneralInfoAdapter.swapCursor(data);
            }
            break;
            case ABSENT_CLASSES_LOADER_ID: {
                if (data.getCount() > 0) {
                    mMergeAdapter.setActive(mAbsentClassesHeader, true);
                }
                mAbsentClassesAdapter.swapCursor(data);
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
            case ABSENT_CLASSES_LOADER_ID: {
                mAbsentClassesAdapter.swapCursor(null);
            }
            break;
        }
    }

}
