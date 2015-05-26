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
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
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

/**
 * Represents the general vertretungsplan view of the app.
 */
public class GeneralVertretungsplanActivity extends
        BaseVertretungsplanActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbar.setTitle(getResources().getStringArray(R.array
                .drawer_titles)[1]);
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
    }
}
