package de.itgdah.vertretungsplan;

import android.app.Application;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.provider.ContactsContract;
import android.test.ApplicationTestCase;
import android.util.Log;

import junit.framework.TestResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

import de.itgdah.vertretungsplan.data.VertretungsplanContract;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class TestContentProvider extends ApplicationTestCase<Application> {

    public static final String LOG_TAG = TestContentProvider.class.getSimpleName();

    public TestContentProvider() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testContentProviderDates() {
        Cursor c = mContext.getContentResolver().query(VertretungsplanContract.Days
               .CONTENT_URI, null, null, null, null);
       assertEquals(3, c.getCount());
    }

    public void testContentProviderVertretungen() {
        if (mContext.getContentResolver().query(VertretungsplanContract.Vertretungen.CONTENT_URI,
                null, null, null, null).getCount() == 0) {
            fail("No vertretungsplan entries.");
        }

    }
}
