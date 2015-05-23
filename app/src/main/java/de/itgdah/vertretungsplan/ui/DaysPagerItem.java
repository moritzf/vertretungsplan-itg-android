package de.itgdah.vertretungsplan.ui;

import android.app.Fragment;

/**
 * Represents a tab displayed by {@link android.support.v4.view.ViewPager}.
 */
public class DaysPagerItem {
    private final CharSequence mTitle;
    private final int mIndicatorColor;

    public DaysPagerItem(CharSequence title, int indicatorColor) {
        mTitle = title;
        mIndicatorColor = indicatorColor;
    }

    /**
     * Returns a new {@link android.support.v4.app.ListFragment} to be
     * displayed by the {@link android.support.v4.view.ViewPager}
     */
    DayVertretungsplanListFragment createFragment() { return DayVertretungsplanListFragment
            .newInstance(); };

    /**
     * @return the title which represents this tab.
     */
    CharSequence getTitle() { return mTitle; }

    int getmIndicatorColor() { return mIndicatorColor; }

}
