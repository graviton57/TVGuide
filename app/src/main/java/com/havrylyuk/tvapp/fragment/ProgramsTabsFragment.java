package com.havrylyuk.tvapp.fragment;


import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.havrylyuk.tvapp.R;
import com.havrylyuk.tvapp.activity.MainActivity;
import com.havrylyuk.tvapp.adapter.ChannelsViewPagerAdapter;
import com.havrylyuk.tvapp.data.local.TvContract.ChannelEntry;
import com.havrylyuk.tvapp.util.PreferencesHelper;
import com.havrylyuk.tvapp.util.Utility;

import java.text.SimpleDateFormat;
import java.util.Locale;


/**
 *
 * Created by Igor Havrylyuk on 19.02.2017.
 */

public class ProgramsTabsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String MAIN_FRAGMENT_TAG = "com.havrylyuk.tvapp.MAIN_FRAGMENT_TAG";

    private static final int CONTENT_LOADER = 1005;
    private static final String SELECTED_TAB_ITEM = "com.havrylyuk.tvapp.SELECTED_TAB_ITEM";
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private String sortBy;
    private PreferencesHelper preferencesHelper;
    private ChannelsViewPagerAdapter viewPagerAdapter;
    private int selectedTabPosition;

    public static Fragment newInstance() {
        return new ProgramsTabsFragment();
    }

    public ProgramsTabsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferencesHelper= PreferencesHelper.getInstance();
        sortBy = preferencesHelper.getChannelSortType(getString(R.string.pref_sort_channel_key));
        if (savedInstanceState != null) {
            selectedTabPosition = savedInstanceState.getInt(SELECTED_TAB_ITEM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
            return inflater.inflate(R.layout.content_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initTabLayout(view);
        getActivity().getSupportLoaderManager().restartLoader(CONTENT_LOADER, Bundle.EMPTY, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (viewPager != null) {
            outState.putInt(SELECTED_TAB_ITEM, tabLayout.getSelectedTabPosition());
        }
        super.onSaveInstanceState(outState);
    }

    private  void initTabLayout(View view) {
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ((MainActivity)getActivity()).setChannelLogo(
                                viewPagerAdapter.getImagePath(tab.getPosition()));
                viewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == CONTENT_LOADER) {
            return new CursorLoader(
                    getActivity(),
                    ChannelEntry.CONTENT_URI,
                    ChannelFragment.CHANNEL_COLUMNS,
                    null,
                    null,
                    Utility.getPrefSortChannelOrder(sortBy));
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == CONTENT_LOADER) {
            if (data != null && isAdded()) {
                viewPagerAdapter = new ChannelsViewPagerAdapter(getChildFragmentManager());
                long savedDate = preferencesHelper.getCurProgramsDate(getActivity().getString(R.string.pref_program_date_key));
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                while (data.moveToNext()) {
                    viewPagerAdapter.addFragment(
                            ProgramFragment.newInstance(
                            data.getLong(ChannelFragment.COL_CH_ID), format.format(savedDate)),
                            data.getString(ChannelFragment.COL_NAME),
                            data.getString(ChannelFragment.COL_IMAGE)
                    );
                }
                if (viewPager != null) {
                    viewPager.setAdapter(viewPagerAdapter);
                    tabLayout.setupWithViewPager(viewPager);
                    viewPager.setCurrentItem(selectedTabPosition);
                }
            }
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void sortChannels(String value) {
        sortBy = value;
        getActivity().getSupportLoaderManager().restartLoader(CONTENT_LOADER, Bundle.EMPTY, this);
    }

}
