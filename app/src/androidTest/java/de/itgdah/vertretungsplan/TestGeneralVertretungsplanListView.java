package de.itgdah.vertretungsplan;

import android.app.Fragment;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

/**
 * Created by Moritz on 5/22/2015.
 */
public class TestGeneralVertretungsplanListView extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mMainActivity;
    private Fragment mVertretungsplanFragment;
    private ListView mListView;

    public TestGeneralVertretungsplanListView() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMainActivity = getActivity();
        mVertretungsplanFragment = getActivity().getFragmentManager().findFragmentById(R.id
                .main_contentframe);

    }

    public void testListView() {
        mListView = (ListView) mVertretungsplanFragment.getView().findViewById(R.id
                .vertretungsplan_listview);
        if(mListView.getCount() == 0) {
            fail("List view contains no items.");
        }
    }
}
