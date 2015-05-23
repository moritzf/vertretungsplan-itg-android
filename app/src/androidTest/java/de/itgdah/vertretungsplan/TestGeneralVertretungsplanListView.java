package de.itgdah.vertretungsplan;

import android.app.Fragment;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

import de.itgdah.vertretungsplan.ui.AllgVertretungsplanActivity;

/**
 * Tests the list view of the general vertretungsplan
 */
public class TestGeneralVertretungsplanListView extends ActivityInstrumentationTestCase2<AllgVertretungsplanActivity> {

    private Fragment mVertretungsplanFragment;
    private ListView mListView;

    public TestGeneralVertretungsplanListView() {
        super(AllgVertretungsplanActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

    }

}
