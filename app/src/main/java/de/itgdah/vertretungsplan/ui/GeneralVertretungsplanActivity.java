package de.itgdah.vertretungsplan.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
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
 * Represents the general vertretungsplan view of the app.
 */
public class GeneralVertretungsplanActivity extends
        BaseVertretungsplanActivity {

    public GeneralDaysPagerAdapter mDaysPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mDrawerPositionSelf = 0;
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initAdapter() {
        mDaysPagerAdapter = new GeneralDaysPagerAdapter(getFragmentManager());
        mDaysPager.setAdapter(mDaysPagerAdapter);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        super.onLoadFinished(loader, data);
        if (data.getCount() > 0) {
            mDaysPagerAdapter.notifyDataSetChanged();
        }
    }

    public class GeneralDaysPagerAdapter extends BaseDaysPagerAdapter {
        public GeneralDaysPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mDaysTabs.get(position).getTitle();
        }

        @Override
        public Fragment getItem(int position) {
            GeneralDayListFragment fragment = new GeneralDayListFragment();
            fragment.setArguments(super.createBundle(position, dateIdsArray));
            return fragment;
        }
    }
}
