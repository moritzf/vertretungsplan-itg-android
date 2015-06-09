package de.itgdah.vertretungsplan.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import de.itgdah.vertretungsplan.R;

/**
 * This class serves as a base for all activities. It contains the navigation
 * drawer and the toolbar.
 */
public class BaseActivity extends AppCompatActivity {

    public static final String SHARED_PREFERENCES_FILENAME = "vertretungsplan" +
            ".preferences";
    // drawer related
    public String[] mTitles;
    public DrawerLayout mDrawerLayout;
    public ListView mDrawerList;
    public RelativeLayout mDrawer;
    public Toolbar mToolbar;
    public ActionBarDrawerToggle mDrawerToggle;
    public int mDrawerPositionSelf = 0; // default value that's overrriden in
    // inheriting activities.

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);

        mTitles = getResources().getStringArray(R.array.drawer_titles);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_base_activity);
        mDrawerList = (ListView) findViewById(R.id.drawer_list);
        mDrawer = (RelativeLayout) findViewById(R.id.main_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, mTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar , R
                .string.drawer_open, R.string.drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

    }

    private void selectItem(int position) {

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        if (position != mDrawerPositionSelf) {
            switch (position) {
                case 0: {
                    startActivity(new Intent(this,
                            GeneralVertretungsplanActivity.class)); } break;
                case 1: { startActivity(new Intent(this,
                        MyVertretungsplanActivity.class)); } break;
            }
        }
        mDrawerLayout.closeDrawer(mDrawer);
    }

    private class DrawerItemClickListener implements AdapterView.OnItemClickListener {

        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }
}
