package de.itgdah.vertretungsplan.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;

/**
 * Adapter for the Days Pager.
 */
public abstract class BaseDaysPagerAdapter extends FragmentPagerAdapter {
    public BaseDaysPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public int NUM_DAYS_IN_PAGER = 3;

    @Override
    public int getCount() {
        return NUM_DAYS_IN_PAGER;
    }

    @Override
    public abstract CharSequence getPageTitle(int position);

    @Override
    public abstract Fragment getItem(int position);

    public Bundle createBundle(int position, String[] dateIdsArray) {
        Bundle bundle = new Bundle();
        bundle.putString(BaseDayListFragment.DAY_ID_KEY,
                dateIdsArray[position]);
        return bundle;
    }
}
