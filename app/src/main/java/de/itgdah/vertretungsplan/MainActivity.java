package de.itgdah.vertretungsplan;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import de.itgdah.vertretungsplan.data.VertretungsplanContract;
import de.itgdah.vertretungsplan.web.FetchVertretungsplanTask;


public class MainActivity extends Activity {

    public String[] mTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    public static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        mTitles = getResources().getStringArray(R.array.drawer_titles);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_activity);
        mDrawerList = (ListView) findViewById(R.id.main_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class VertretungsplanFragment extends Fragment implements LoaderManager
            .LoaderCallbacks<Cursor> {

        private static final String LOG_TAG = VertretungsplanFragment.class.getSimpleName();
        private SimpleCursorAdapter mVertretungsplanAdapter;

        public VertretungsplanFragment() {
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState){
            super.onActivityCreated(savedInstanceState);
            setHasOptionsMenu(true);
            Toolbar toolbar=(Toolbar) getActivity().findViewById(R.id.toolbar);
            toolbar.inflateMenu(R.menu.menu_main);
            toolbar.setTitle(getResources().getStringArray(R.array.drawer_titles)[0]);
            toolbar.setTitleTextColor(Color.WHITE);
            getLoaderManager().initLoader(0, null,this);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putBoolean("running", true);
        }

        @Override
        public void onViewStateRestored(Bundle savedInstanceState) {
            super.onViewStateRestored(savedInstanceState);
            mVertretungsplanAdapter.changeCursor(mContext.getContentResolver().query
                    (VertretungsplanContract.Vertretungen.CONTENT_URI, null, null, null,
                            null));
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            String[] mVertretungsplanListColumns = {
                    VertretungsplanContract.Vertretungen.COLUMN_PERIOD,
                    VertretungsplanContract.Vertretungen.COLUMN_CLASS,
                    VertretungsplanContract.Vertretungen.COLUMN_SUBJECT,
                    VertretungsplanContract.Vertretungen.COLUMN_COMMENT,
                    VertretungsplanContract.Vertretungen._ID
            };

            int[] mVertretungsplanListItems = {
                    R.id.textView, R.id.textView2, R.id.textView3,
                    R.id.textView5
            };

            String mSelectionClause = null;
            String[] mSelectionArgs = null;
            Cursor mCursor = getActivity().getContentResolver().query(
                    VertretungsplanContract.Vertretungen.CONTENT_URI,
                    mVertretungsplanListColumns,
                    mSelectionClause,
                    mSelectionArgs,
                    null
            );
            mVertretungsplanAdapter = new SimpleCursorAdapter(
                    getActivity(),
                    R.layout.main_fragment_vertretungsplan_listitem,
                    mCursor,
                    mVertretungsplanListColumns, // column names
                    mVertretungsplanListItems, // view ids
                    0);

            if(savedInstanceState == null) {
                Log.v(LOG_TAG, "Execute FetchWeatherTask");
                updateVertretungsplanList();
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.main_fragment_vertretungsplan, container, false);
            ListView vertretungsplanView = (ListView) rootView.findViewById(R.id.vertretungsplan_listview);
            vertretungsplanView.setAdapter(mVertretungsplanAdapter);
            return rootView;
        }

        public void updateVertretungsplanList() {
                FetchVertretungsplanTask task = new FetchVertretungsplanTask(mContext);
                task.execute();
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(), VertretungsplanContract.Vertretungen
                    .CONTENT_URI, null, null, null, VertretungsplanContract.Vertretungen
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
