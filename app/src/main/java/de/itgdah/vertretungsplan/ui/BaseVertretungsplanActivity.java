package de.itgdah.vertretungsplan.ui;

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
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import de.itgdah.vertretungsplan.R;
import de.itgdah.vertretungsplan.data.VertretungsplanContract;
import de.itgdah.vertretungsplan.sync.VertretungsplanSyncAdapter;
import de.itgdah.vertretungsplan.ui.widget.DaysPagerTab;
import de.itgdah.vertretungsplan.ui.widget.SlidingTabLayout;
import de.itgdah.vertretungsplan.util.Utility;

/**
 * Serves as a base class for the general and personal vertretungsplan.
 */
public class BaseVertretungsplanActivity extends BaseActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DAYS_LOADER_ID = 3;
    public static String[] dateIdsArray = new String[3];

    // tabs related
    public ViewPager mDaysPager;
    SlidingTabLayout mSlidingTabLayout;
    List<DaysPagerTab> mDaysTabs = new ArrayList<>();

    // necessary for restartLoader in onReceive.
    private BaseVertretungsplanActivity mHandle = this;
    private final BroadcastReceiver syncFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getLoaderManager().restartLoader(DAYS_LOADER_ID, null, mHandle);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_base_vertretungsplan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.refresh_menu_item) {
            VertretungsplanSyncAdapter.syncImmediately(this);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            VertretungsplanSyncAdapter.initializeSyncAdapter(this);
        }
        findViewById(R.id.sliding_tabs_stub).setVisibility(View.VISIBLE);
        findViewById(R.id.vertretungsplan_days_pager_stub).setVisibility(View
                .VISIBLE);

        mDaysPager = (ViewPager) findViewById(R.id
                .vertretungsplan_days_pager);
        initAdapter();

        getLoaderManager().initLoader(DAYS_LOADER_ID, null, this);

        // placeholder tabs
        mDaysTabs.add(new DaysPagerTab(""));
        mDaysTabs.add(new DaysPagerTab(""));
        mDaysTabs.add(new DaysPagerTab(""));

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.sliding_tabs_day_tab, android.R.id
                .text1);
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setSelectedIndicatorColors(Color.WHITE);

        mSlidingTabLayout.setViewPager(mDaysPager);

        mToolbar.setTitle(getResources().getStringArray(R.array
                .drawer_titles)[mDrawerPositionSelf]);
        setSupportActionBar(mToolbar);
    }

    public void initAdapter() {
    }

    @Override
    protected void onResume() {
        super.onPostResume();
        registerReceiver(syncFinishedReceiver, new IntentFilter
                (VertretungsplanSyncAdapter.SYNC_FINISHED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(syncFinishedReceiver);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, VertretungsplanContract.Days.CONTENT_URI,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        for (int i = 0; i < data.getCount(); i++) {
            data.moveToPosition(i);
            dateIdsArray[i] = data.getString(0);
        }
        if (data.getCount() > 0) {
            for (int i = 0; i < data.getCount(); i++) {
                data.moveToPosition(i);
                mDaysTabs.set(i, new DaysPagerTab(Utility
                        .getDayOfTheWeekFromDate(Utility.getDateFromDb(data
                                .getString(1)))));
            }
            mSlidingTabLayout.setViewPager(mDaysPager);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
