package de.itgdah.vertretungsplan.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import de.itgdah.vertretungsplan.R;

/**
 * Created by moritz on 26.05.15.
 */
public class MyVertretungsplanActivity extends BaseVertretungsplanActivity {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.getItem(1).setVisible(true);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.my_data_menu_item) {
            startActivity(new Intent(this, MyDataActivity.class));
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mDrawerPositionSelf = 1;
        super.onCreate(savedInstanceState);
        mDaysPagerAdapter = new MyDaysPagerAdapter(getFragmentManager());
        mDaysPager.setAdapter(mDaysPagerAdapter);
        mSlidingTabLayout.setViewPager(mDaysPager);

        mToolbar.setTitle(getResources().getStringArray(R.array
                .drawer_titles)[1]);
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
    }

    public class MyDaysPagerAdapter extends BaseVertretungsplanActivity
            .BaseDaysPagerAdapter {
        public MyDaysPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment dayListFragment = new MyDayListFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(MyDayListFragment.PAGER_POSITION_KEY, position);
            dayListFragment.setArguments(bundle);
            return dayListFragment;
        }
    }
}
