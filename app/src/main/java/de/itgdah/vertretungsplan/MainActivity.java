package de.itgdah.vertretungsplan;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import de.itgdah.vertretungsplan.sync.VertretungsplanSyncAdapter;


public class MainActivity extends Activity {

    public String[] mTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    public static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mTitles = getResources().getStringArray(R.array.drawer_titles);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_activity);
        mDrawerList = (ListView) findViewById(R.id.main_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, mTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        if(savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.main_contentframe, new VertretungsplanFragment())
                    .commit();
        }
        mContext = getApplicationContext();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Toolbar is used instead.
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Toolbar is used instead.
        return false;
    }

    private class DrawerItemClickListener implements android.widget.AdapterView.OnItemClickListener {

        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        // Create a new fragment and specify the planet to show based on position
        Fragment fragment = new VertretungsplanFragment();
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.main_contentframe, fragment)
                .commit();

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);

        mDrawerLayout.closeDrawer(mDrawerList);
    }
}
