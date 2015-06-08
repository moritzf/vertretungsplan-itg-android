package de.itgdah.vertretungsplan.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import de.itgdah.vertretungsplan.R;
import de.itgdah.vertretungsplan.sync.VertretungsplanSyncAdapter;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.Days;
import de.itgdah.vertretungsplan.ui.widget.DaysPagerTab;
import de.itgdah.vertretungsplan.ui.widget.SlidingTabLayout;
import de.itgdah.vertretungsplan.util.Utility;

/**
 * Created by moritz on 26.05.15.
 */
public class MyVertretungsplanActivity extends BaseActivity {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_base_vertretungsplan, menu);
        menu.getItem(1).setVisible(true);
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mDaysPagerAdapter.notifyDataSetChanged();
        mDaysPager.setAdapter(mDaysPagerAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.refresh_menu_item) {
            VertretungsplanSyncAdapter.syncImmediately(this);
            return true;
        }
        if(item.getItemId() == R.id.my_data_menu_item) {
            startActivityForResult(new Intent(this, MyDataActivity.class), 0);
            return true;
        }
        return false;
    }

    // tabs related
    public ViewPager mDaysPager;
    public int NUM_DAYS_IN_PAGER = 3;
    public MyDaysPagerAdapter mDaysPagerAdapter;
    SlidingTabLayout mSlidingTabLayout;
    List<DaysPagerTab> mDaysTabs = new ArrayList<>();

    private final BroadcastReceiver syncFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTabs();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mDrawerPositionSelf = 1;
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null) {
            VertretungsplanSyncAdapter.initializeSyncAdapter(this);
        }
        findViewById(R.id.sliding_tabs_stub).setVisibility(View.VISIBLE);
        findViewById(R.id.vertretungsplan_days_pager_stub).setVisibility(View
                .VISIBLE);

        // placeholder tabs
        mDaysTabs.add(new DaysPagerTab(""));
        mDaysTabs.add(new DaysPagerTab(""));
        mDaysTabs.add(new DaysPagerTab(""));

        mDaysPager = (ViewPager) findViewById(R.id
                .vertretungsplan_days_pager);
        mDaysPagerAdapter = new
                MyDaysPagerAdapter(getFragmentManager());
        mDaysPager.setAdapter(mDaysPagerAdapter);

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.sliding_tabs_day_tab, android.R.id
                .text1);
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setSelectedIndicatorColors(Color.WHITE);
        mSlidingTabLayout.setViewPager(mDaysPager);

        mToolbar.setTitle(getResources().getStringArray(R.array
                .drawer_titles)[mDrawerPositionSelf]);
        setSupportActionBar(mToolbar);
        updateTabs();
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

    public boolean updateTabs() {
        boolean success = false;
        Cursor c = getContentResolver().query(Days.CONTENT_URI, new
                String[] {Days.COLUMN_DATE}, null, null, Days._ID + " ASC");
        if (c.getCount() > 0) {
            for (int i = 0; i < NUM_DAYS_IN_PAGER; i++) {
                c.moveToPosition(i);
                mDaysTabs.set(i, new DaysPagerTab(Utility
                        .getDayOfTheWeekFromDate(Utility.getDateFromDb(c
                                .getString(0)))));
            }
            mDaysPagerAdapter.notifyDataSetChanged();
            mSlidingTabLayout.setViewPager(mDaysPager);
            success = true;
        }
        c.close();
        return success;
    }

    /**
     * Adapter for the Days Pager.
     */
    public class MyDaysPagerAdapter extends FragmentPagerAdapter {

        public MyDaysPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_DAYS_IN_PAGER;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mDaysTabs.get(position).getTitle();
        }

        @Override
        public Fragment getItem(int position) {
            Fragment dayListFragment = new MyDayListFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(BaseDayListFragment.PAGER_POSITION_KEY, position);
            dayListFragment.setArguments(bundle);
            return dayListFragment;
        }
    }
}
