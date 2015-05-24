package de.itgdah.vertretungsplan.ui;

import android.app.Fragment;
import android.app.FragmentManager;
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
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;
import java.util.List;

import de.itgdah.vertretungsplan.R;
import de.itgdah.vertretungsplan.data.VertretungsplanContract;
import de.itgdah.vertretungsplan.sync.VertretungsplanSyncAdapter;
import de.itgdah.vertretungsplan.ui.widget.SlidingTabLayout;


public class AllgVertretungsplanActivity extends AppCompatActivity implements
        LoaderManager
        .LoaderCallbacks<Cursor> {


    private static final String LOG_TAG = AllgVertretungsplanActivity.class
            .getSimpleName();

    // required for syncFinishedReceiver
    private final AllgVertretungsplanActivity handle = this;
    private final BroadcastReceiver syncFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(LOG_TAG, "Sync performed.");
            mDaysTabs.set(2, new DaysPagerTab("Date"));
            mDaysPagerAdapter.notifyDataSetChanged();
            mSlidingTabLayout.setViewPager(mDaysPager);
            getLoaderManager().restartLoader(0, null, handle);
        }
    };

    // drawer related
    private String[] mTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;


    public SimpleCursorAdapter mVertretungsplanAdapter;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.allg_vertretungsplan_activity);

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
        VertretungsplanSyncAdapter.syncImmediately(this);

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
                this,
                R.layout.vertretungen_listitem,
                null,
                mVertretungsplanListColumns, // column names
                mVertretungsplanListItems, // view ids
                0);

        Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setTitle(getResources().getStringArray(R.array.drawer_titles)[0]);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        mDaysTabs.add(new DaysPagerTab("test1"));
        mDaysTabs.add(new DaysPagerTab("test2"));
        mDaysTabs.add(new DaysPagerTab("test3"));

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

        getLoaderManager().initLoader(0, null, this);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Cursor c = getContentResolver().query(VertretungsplanContract.Days
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
        return new CursorLoader(this, VertretungsplanContract.Vertretungen
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
            Bundle bundle = new Bundle();
            bundle.putInt("pos", position);
            Fragment dayListFragment = new DayListFragment();
            dayListFragment.setArguments(bundle);
            return dayListFragment;
        }
    }

}
