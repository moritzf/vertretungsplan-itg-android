package de.itgdah.vertretungsplan.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

/**
 * Created by moritz on 23.05.15.
 */
public class VertretungsplanDaysPagerAdapter extends FragmentPagerAdapter {

    public static final int NUM_DAYS_IN_PAGER = 3;

    public VertretungsplanDaysPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return NUM_DAYS_IN_PAGER;
    }

    @Override
    public Fragment getItem(int position) {
        return DayVertretungsplanListFragment.newInstance();
    }
}
