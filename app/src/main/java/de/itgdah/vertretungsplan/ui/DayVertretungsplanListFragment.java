package de.itgdah.vertretungsplan.ui;

import android.app.Fragment;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.itgdah.vertretungsplan.R;

/**
 * Created by moritz on 23.05.15.
 */
public class DayVertretungsplanListFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.vertretungsplan_fragment, container,
                false);
    }

    public static DayVertretungsplanListFragment newInstance() {
        DayVertretungsplanListFragment fragment =  new
                DayVertretungsplanListFragment();
        return fragment;

    }
}
