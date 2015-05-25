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
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.itgdah.vertretungsplan.R;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.Days;
import de.itgdah.vertretungsplan.sync.VertretungsplanSyncAdapter;
import de.itgdah.vertretungsplan.ui.widget.DaysPagerTab;
import de.itgdah.vertretungsplan.ui.widget.SlidingTabLayout;
import de.itgdah.vertretungsplan.util.Utility;


public class AllgVertretungsplanActivity extends AppCompatActivity  {


    private static final String LOG_TAG = AllgVertretungsplanActivity.class
            .getSimpleName();

    // required for syncFinishedReceiver
    private final BroadcastReceiver syncFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(LOG_TAG, "Sync performed.");
            Cursor c = getContentResolver().query(Days.CONTENT_URI, new
                    String[] {Days.COLUMN_DATE}, null, null, Days._ID + " ASC");
            for (int i = 0; i < NUM_DAYS_IN_PAGER; i++) {
                c.moveToPosition(i);
               mDaysTabs.set(i, new DaysPagerTab(Utility
                       .getDayOfTheWeekFromDate(Utility.getDateFromDb(c
                               .getString(0)))));
            }
            c.close();
            mDaysPagerAdapter.notifyDataSetChanged();
            mSlidingTabLayout.setViewPager(mDaysPager);
        }
    };

    // drawer related
    private String[] mTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;


    // tabs related
    public ViewPager mDaysPager;
    SlidingTabLayout mSlidingTabLayout;
    List<DaysPagerTab> mDaysTabs = new ArrayList<>();
    public int NUM_DAYS_IN_PAGER = 3;
    public DaysPagerAdapter mDaysPagerAdapter;

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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.allg_vertretungsplan_activity);
        VertretungsplanSyncAdapter.syncImmediately(this);
        mTitles = getResources().getStringArray(R.array.drawer_titles);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_activity);
        mDrawerList = (ListView) findViewById(R.id.main_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, mTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        if(savedInstanceState == null) {
            VertretungsplanSyncAdapter.initializeSyncAdapter(this);
        }


        Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getStringArray(R.array.drawer_titles)[0]);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        mDaysTabs.add(new DaysPagerTab(""));
        mDaysTabs.add(new DaysPagerTab(""));
        mDaysTabs.add(new DaysPagerTab(""));

        mDaysPager = (ViewPager) findViewById(R.id
                .vertretungsplan_days_pager);
        mDaysPagerAdapter = new
                DaysPagerAdapter(getFragmentManager());
        mDaysPager.setAdapter(mDaysPagerAdapter);

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.days_tab, android.R.id
                .text1);
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setSelectedIndicatorColors(Color.WHITE);
        mSlidingTabLayout.setViewPager(mDaysPager);

    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        // Create a new fragment and specify the planet to show based on position

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);

        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private class DrawerItemClickListener implements android.widget.AdapterView.OnItemClickListener {

        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }


    /**
     * Created by moritz on 23.05.15.
     */
    public class DaysPagerAdapter extends FragmentPagerAdapter {

        public DaysPagerAdapter(FragmentManager fm) {
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
            Fragment dayListFragment = new DayListFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(DayListFragment.PAGER_POSITION_KEY, position);
            dayListFragment.setArguments(bundle);
            return dayListFragment;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.refresh_menu_item) {
            VertretungsplanSyncAdapter.syncImmediately(this);
            return true;
        }
        return false;

    }
}
