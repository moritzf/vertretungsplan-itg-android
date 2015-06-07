package de.itgdah.vertretungsplan.ui;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.itgdah.vertretungsplan.R;
import de.itgdah.vertretungsplan.sync.VertretungsplanSyncAdapter;

/**
 * Activity for user data input for the personal vertretungsplan.
 */
public class MyDataActivity extends BaseActivity {

    /* stores the class that the user specified */
    public static final String CLASS_OF_USER = "class_of_user";
    public static final String SUBJECTS_OF_USER = "subjects_of_user";
    private EditText mSubjectInput;
    private EditText mClassInput;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_my_data, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.my_data_done_item) {

            SharedPreferences preferences = getSharedPreferences
                    (BaseActivity.SHARED_PREFERENCES_FILENAME,
                            MODE_PRIVATE);

            String classUser = mClassInput.getText().toString();
            if (!classUser.isEmpty() ) {
                if (validateSubjectsAndClassesUser(classUser)) {
                    preferences.edit().putString(CLASS_OF_USER, classUser).apply();
                }
            } else {
                Toast.makeText(this, R.string.class_cannot_be_empty, Toast
                        .LENGTH_SHORT).show();
            }

            String subjectsUser = mSubjectInput.getText().toString();
            if (subjectsUser.isEmpty() || validateSubjectsAndClassesUser
                    (subjectsUser)) {
                preferences.edit().putString(SUBJECTS_OF_USER, subjectsUser)
                        .apply();
            }
            // forces data reload
            VertretungsplanSyncAdapter.syncImmediately
                    (getApplicationContext());
            finish();
            return true;
        }
        return false;
    }

    private boolean validateSubjectsAndClassesUser(String subjectsUser) {
        String subjectsUserStrippedOfWhitespace = subjectsUser.replaceAll
                ("\\s+", "");
        Pattern p = Pattern.compile("\\w+(,\\w+)*");
        Matcher m = p.matcher(subjectsUserStrippedOfWhitespace);
        return m.matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewById(R.id.my_data_fragment_stub).setVisibility(View.VISIBLE);
        mClassInput = (EditText) findViewById(R.id
                .my_data_class_input);
        mSubjectInput = (EditText) findViewById(R.id
                .my_data_subject_input);
        SharedPreferences preferences = getSharedPreferences(BaseActivity
                .SHARED_PREFERENCES_FILENAME, MODE_PRIVATE);
        String classOfUser = preferences.getString(CLASS_OF_USER, "");
        mClassInput.setText(classOfUser);
        String subjectsOfUser = preferences.getString(SUBJECTS_OF_USER, "");
        mSubjectInput.setText(subjectsOfUser);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mToolbar.setTitle(R.string.my_data);
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);

    }
}
