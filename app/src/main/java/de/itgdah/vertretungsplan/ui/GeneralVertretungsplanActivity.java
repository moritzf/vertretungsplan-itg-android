package de.itgdah.vertretungsplan.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;

import de.itgdah.vertretungsplan.R;

/**
 * Represents the general vertretungsplan view of the app.
 */
public class GeneralVertretungsplanActivity extends
        BaseVertretungsplanActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mDrawerPositionSelf = 0;
        super.onCreate(savedInstanceState);
        mDaysPagerAdapter = new GeneralDaysPagerAdapter(getFragmentManager());
        mDaysPager.setAdapter(mDaysPagerAdapter);
        mSlidingTabLayout.setViewPager(mDaysPager);

        mToolbar.setTitle(getResources().getStringArray(R.array
                .drawer_titles)[0]);
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
    }

    public class GeneralDaysPagerAdapter extends BaseVertretungsplanActivity
            .BaseDaysPagerAdapter {
        public GeneralDaysPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment dayListFragment = new GeneralDayListFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(GeneralDayListFragment.PAGER_POSITION_KEY, position);
            dayListFragment.setArguments(bundle);
            return dayListFragment;
        }
    }
}
