package de.itgdah.vertretungsplan.ui;

/**
 * Represents a tab displayed by {@link android.support.v4.view.ViewPager}.
 */
public class DaysPagerTab {
    private final CharSequence mTitle;

    public DaysPagerTab(CharSequence title) {
        mTitle = title;
    }

    /**
     * Returns a new {@link android.support.v4.app.ListFragment} to be
     * displayed by the {@link android.support.v4.view.ViewPager}
     */
    DayListFragment createFragment() { return new
            DayListFragment(); };

    /**
     * @return the title which represents this tab.
     */
    CharSequence getTitle() { return mTitle; }

}
