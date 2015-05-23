package de.itgdah.vertretungsplan;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.test.ApplicationTestCase;

import de.itgdah.vertretungsplan.data.VertretungsplanContract.Vertretungen;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.Days;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.PersonalData;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class TestContentProvider extends ApplicationTestCase<Application> {

    public static final String LOG_TAG = TestContentProvider.class.getSimpleName();

    public TestContentProvider() {
        super(Application.class);
    }

    public void testDates() {
        Cursor c = mContext.getContentResolver().query(Days
               .CONTENT_URI, null, null, null, null);
        c.close();
       assertEquals(3, c.getCount());
    }

    public void testVertretungen() {
        if (mContext.getContentResolver().query(Vertretungen.CONTENT_URI,
                null, null, null, null).getCount() == 0) {
            fail("No vertretungsplan entries.");
        }

    }

    public void testPersonalData() {
        mContext.getContentResolver().delete(PersonalData.CONTENT_URI, null, null);
        ContentValues cv = new ContentValues();
        cv.put(PersonalData.COLUMN_SUBJECT, "1sk4");
        cv.put(PersonalData.COLUMN_CLASS, "11");
        mContext.getContentResolver().insert(PersonalData.CONTENT_URI, cv);
        Cursor c = mContext.getContentResolver().query(PersonalData.CONTENT_URI, null, null,
                null, null);
        assertEquals(1, c.getCount());
    }
}
