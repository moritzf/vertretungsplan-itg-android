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
import de.itgdah.vertretungsplan.data.VertretungsplanContract.Days;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.GeneralInfo;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.Vertretungen;
import de.itgdah.vertretungsplan.sync.VertretungsplanSyncAdapter;

/**
 * Base fragment that contains common parts of the vertretungsplan list view,
 * i.e. vertretungen and general infos.
 */
public class BaseDayListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /* Used in DaysPagerAdapter and denotes the position of this fragment in
    the view pager
     */
    public static final String PAGER_POSITION_KEY = "pos";

    private static final String LOG_TAG = GeneralDayListFragment.class.getSimpleName();
    public static final int VERTRETUNGEN_LOADER_ID = 0;
    private static final int GENERAL_INFO_LOADER_ID = 1;
    private static final int ABSENT_CLASSES_LOADER_ID = 2;
    private static final int DAYS_LOADER_ID = 3;
    /**
     * Stores the dateIds of the days in the vertretungsplan table. Used for
     * filtering by date in onCreateLoader.
     */
    private static String[] dateIdsArray = new String[3];
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

            getLoaderManager().restartLoader(DAYS_LOADER_ID, null, mHandle);
            getLoaderManager().restartLoader(VERTRETUNGEN_LOADER_ID, null, mHandle);
            getLoaderManager().restartLoader(GENERAL_INFO_LOADER_ID, null, mHandle);
            getLoaderManager().restartLoader(ABSENT_CLASSES_LOADER_ID, null, mHandle);

        }
    };

    private View mVertretungenHeader;
    private View mGeneralInfoHeader;
    private String[] mSelectionArgs = null;
    private View mAbsentClassesHeader;
    public String mSelection = Vertretungen.COLUMN_DAYS_KEY + " = ? ";
    /* Handles the filtering in myVertretungsplan */
    public String mCustomSelection = null;
    public String[] mCustomSelectionArgs = null;

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(syncFinishedReceiver);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLoaderManager().restartLoader(DAYS_LOADER_ID, null, mHandle);
        getLoaderManager().restartLoader(VERTRETUNGEN_LOADER_ID, null, mHandle);
        getLoaderManager().restartLoader(GENERAL_INFO_LOADER_ID, null, mHandle);
        getLoaderManager().restartLoader(ABSENT_CLASSES_LOADER_ID, null, mHandle);
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
        mMergeAdapter.addAdapter(mVertretungenAdapter);
        mMergeAdapter.setActive(mVertretungenHeader, false);
        mMergeAdapter.addView(mGeneralInfoHeader);
        mMergeAdapter.setActive(mGeneralInfoHeader, false);
        mMergeAdapter.addAdapter(mGeneralInfoAdapter);
        mMergeAdapter.addView(mAbsentClassesHeader);
        mMergeAdapter.setActive(mAbsentClassesHeader, false);
        mMergeAdapter.addAdapter(mAbsentClassesAdapter);
        setListAdapter(mMergeAdapter);

        getLoaderManager().initLoader(DAYS_LOADER_ID, null, this);
        getLoaderManager().initLoader(GENERAL_INFO_LOADER_ID, null, this);
        getLoaderManager().initLoader(VERTRETUNGEN_LOADER_ID, null, this);
        getLoaderManager().initLoader(ABSENT_CLASSES_LOADER_ID, null, this);
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        int position = getArguments().getInt(PAGER_POSITION_KEY);
        // check if dates are available
        Cursor c = getActivity().getContentResolver().query(Days.CONTENT_URI,
                null, null, null, null);
        for (int i = 0; i < c.getCount(); i++) {
            c.moveToPosition(i);
            dateIdsArray[i] = c.getString(0);
        }
        c.close();
        String dayId = dateIdsArray[0] != null ? dateIdsArray[position] : "";
        // if the dayIds are available, select date id that corresponds
            // to the position in the pager.
            mSelectionArgs = new String[]{dayId};
        switch (id) {
            case VERTRETUNGEN_LOADER_ID: {
                if (mCustomSelection != null) {
                    // assumes that mCustomSelection and mCustomSelectionArgs are
                    // both initialized
                    mCustomSelectionArgs[mCustomSelectionArgs.length - 1] = dayId;
                    return new CursorLoader(getActivity(), Vertretungen
                            .CONTENT_URI, null, mCustomSelection,
                            mCustomSelectionArgs,
                            null);
                } else {
                    return new CursorLoader(getActivity(), Vertretungen
                            .CONTENT_URI, null, mSelection,
                            mSelectionArgs,
                            null);
                }
            }
            case GENERAL_INFO_LOADER_ID: {
                return new CursorLoader(getActivity(),
                        GeneralInfo.CONTENT_URI,
                        null, mSelection, mSelectionArgs,
                        GeneralInfo._ID + " ASC");
            }
            case ABSENT_CLASSES_LOADER_ID: {
                return new CursorLoader(getActivity(), AbsentClasses
                        .CONTENT_URI, null, mSelection, mSelectionArgs,
                        null);
            }
            case DAYS_LOADER_ID: {
                return new CursorLoader(getActivity(), Days.CONTENT_URI,
                        null, null, null, null);
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
                } else {
                    mMergeAdapter.setActive(mVertretungenHeader, false);
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
            case DAYS_LOADER_ID: {
/*             populates the dateIdsArray. This logic is necessary as we
             can't depend on the date table being already populated in
             onCreateLoader. As the date ids are necessary for filtering, we
             fetch them after the sync with the server has been completed,
             i.e. onPerformSync has finished.*/
                for (int i = 0; i < data.getCount(); i++) {
                    data.moveToPosition(i);
                    dateIdsArray[i] = data.getString(0);
                }
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
