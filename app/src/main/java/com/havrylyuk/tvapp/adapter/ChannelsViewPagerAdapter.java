package com.havrylyuk.tvapp.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * Created by Igor Havrylyuk on 20.02.2017.
 */

public class ChannelsViewPagerAdapter extends FragmentStatePagerAdapter {


    private final List<Fragment> fragmentList = new ArrayList<>();
    private final List<String> fragmentTitleList = new ArrayList<>();
    private final List<String> fragmentImageList = new ArrayList<>();

    public ChannelsViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentTitleList.get(position);
    }

    public void addFragment(Fragment fragment, String title, String imagePath) {
        fragmentList.add(fragment);
        fragmentTitleList.add(title);
        fragmentImageList.add(imagePath);
    }

    public void addImagePath(String imageName) {
        fragmentImageList.add(imageName);
    }

    public String getImagePath(int position) {
       return fragmentImageList.get(position);
    }

}
