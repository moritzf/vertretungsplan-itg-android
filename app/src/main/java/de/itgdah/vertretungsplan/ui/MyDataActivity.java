package de.itgdah.vertretungsplan.ui;

import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;

import de.itgdah.vertretungsplan.R;

/**
 * Activity for user data input for the personal vertretungsplan.
 */
public class MyDataActivity extends BaseActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_my_data, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.my_data_done_item) {
            finish();
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewById(R.id.my_data_fragment_stub).setVisibility(View.VISIBLE);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mToolbar.setTitle(R.string.my_data);
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);

    }
}
