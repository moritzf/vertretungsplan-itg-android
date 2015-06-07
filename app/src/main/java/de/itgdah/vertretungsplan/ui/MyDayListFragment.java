package de.itgdah.vertretungsplan.ui;

import android.content.Context;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import de.itgdah.vertretungsplan.data.VertretungsplanContract;

/**
 * Created by moritz on 26.05.15.
 */
public class MyDayListFragment extends BaseDayListFragment {
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        SharedPreferences preferences = getActivity().getSharedPreferences
                (BaseActivity.SHARED_PREFERENCES_FILENAME, Context
                        .MODE_PRIVATE);
        String classOfUser = preferences.getString(MyDataActivity
                .CLASS_OF_USER, "");
        String subjectsOfUser = preferences.getString(MyDataActivity
                .SUBJECTS_OF_USER, "");
        if (classOfUser != null && subjectsOfUser != null) {
            if (id == BaseDayListFragment.VERTRETUNGEN_LOADER_ID && !classOfUser
                    .isEmpty() && !subjectsOfUser.isEmpty()) {
                String[] subjectsArray = subjectsOfUser.split("\\s*,\\s*");
                String inClauseContent = "";
                for(int i = 0; i < subjectsArray.length; i++) {
                    if(i>0) {
                        inClauseContent += ",";
                    }
                   inClauseContent += "? ";
                }
                mCustomSelection = VertretungsplanContract.Vertretungen
                        .COLUMN_CLASS + " = ?  AND " + VertretungsplanContract
                        .Vertretungen.COLUMN_SUBJECT + " IN ( " +
                        inClauseContent +" ) ";
                mCustomSelection += " AND " + mSelection;

                mCustomSelectionArgs = new String[2 + subjectsArray.length];
                // class, N subjects, day_id
                mCustomSelectionArgs[0] = classOfUser;
                for (int i = 0; i < subjectsArray.length; i++) {
                   mCustomSelectionArgs[i + 1] = subjectsArray[i];
                }
            }
        }
        return super.onCreateLoader(id, args);
    }
}
