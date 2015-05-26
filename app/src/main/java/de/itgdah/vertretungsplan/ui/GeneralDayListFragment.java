package de.itgdah.vertretungsplan.ui;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;

import com.commonsware.cwac.merge.MergeAdapter;

import de.itgdah.vertretungsplan.R;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.Vertretungen;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.GeneralInfo;
import de.itgdah.vertretungsplan.data.VertretungsplanContract.Days;
import de.itgdah.vertretungsplan.sync.VertretungsplanSyncAdapter;

/**
 * Represents one day of the vertretungsplan containing the vertretungen and
 * the general info of the day. This class is used in the view pager and it's
 * the content frame of the selected day.
 */
public class GeneralDayListFragment extends BaseDayListFragment {
}
