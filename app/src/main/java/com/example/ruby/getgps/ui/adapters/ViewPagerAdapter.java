package com.example.ruby.getgps.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.ruby.getgps.ui.activities.MainActivity;
import com.example.ruby.getgps.ui.fragments.HomeTabFragment;
import com.example.ruby.getgps.ui.fragments.TripTabFragment;

/**
 * Adapter for ViewPager in the MainActivity.
 *
 * @see android.support.v4.view.ViewPager
 * @see FragmentPagerAdapter
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {
    private final int PAGE_COUNT = 2;
    private final String[] tabTitles = new String[2];
    private final MainActivity mainActivity;

    public ViewPagerAdapter(FragmentManager fm, String tab1, String tab2, MainActivity mainActivity) {
        super(fm);
        tabTitles[0] = tab1;
        tabTitles[1] = tab2;
        this.mainActivity = mainActivity; //TODO: check its uses
    }

    /**
     * @return number of views available.
     */
    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    /**
     * Returns the respective page content
     *
     * @param position page selected
     * @return fragment to be shown
     */
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return HomeTabFragment.getInstance();
        } else {
            return TripTabFragment.getInstance();
        }
    }

    /**
     * This method may be called by the ViewPager to obtain a title string to describe the specified page.
     * his method may return null indicating no title for this page. The default implementation returns null.
     *
     * @param position position of the title requested
     * @return title for the requested page
     */
    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];

    }

}
