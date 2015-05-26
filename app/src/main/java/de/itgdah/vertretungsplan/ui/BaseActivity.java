package de.itgdah.vertretungsplan.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import de.itgdah.vertretungsplan.R;

/**
 * This class serves as a base for all activities. It contains the navigation
 * drawer and the toolbar.
 */
public class BaseActivity extends AppCompatActivity {

    // drawer related
    public String[] mTitles;
    public DrawerLayout mDrawerLayout;
    public ListView mDrawerList;
    public Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity);

        mTitles = getResources().getStringArray(R.array.drawer_titles);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_base_activity);
        mDrawerList = (ListView) findViewById(R.id.main_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, mTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());


        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        // Create a new fragment and specify the planet to show based on position

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);

        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private class DrawerItemClickListener implements AdapterView.OnItemClickListener {

        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }
}
