package de.itgdah.vertretungsplan.ui;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.itgdah.vertretungsplan.R;

/**
 * Created by moritz on 23.05.15.
 */
public class DayListFragment extends ListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        int position = getArguments() != null ? getArguments().getInt
                ("pos") : 0;
        return inflater.inflate(R.layout.vertretungsplan_fragment, container,
                false);
    }

}
