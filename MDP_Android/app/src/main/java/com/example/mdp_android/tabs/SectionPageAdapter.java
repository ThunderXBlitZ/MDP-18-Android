package com.example.mdp_android.tabs;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


public class SectionPageAdapter extends FragmentPagerAdapter {

    private int numOfTabs;

    public SectionPageAdapter(FragmentManager fn, int numOfTabs) {
        super(fn);
        this.numOfTabs = numOfTabs;
    }


    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new BluetoothFragment();
            case 1:
                return new MapFragment();
            case 2:
                return new CommFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
