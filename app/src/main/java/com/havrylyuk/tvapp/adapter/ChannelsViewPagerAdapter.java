package com.havrylyuk.tvapp.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.havrylyuk.tvapp.R;
import com.havrylyuk.tvapp.fragment.ProgramFragment;
import com.havrylyuk.tvapp.model.TvChannel;
import com.havrylyuk.tvapp.util.PreferencesHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 *
 * Created by Igor Havrylyuk on 20.02.2017.
 */

public class ChannelsViewPagerAdapter extends FragmentStatePagerAdapter {

    private final List<TvChannel> tvChannels = new ArrayList<>();
    private Context context;
    private PreferencesHelper preferencesHelper;
    private final SimpleDateFormat format;

    public ChannelsViewPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
        preferencesHelper = PreferencesHelper.getInstance();
        format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    public void addItem(TvChannel tvChannel){
        tvChannels.add(tvChannel);
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        long channelId = tvChannels.get(position).getId();
        long savedDate = preferencesHelper.getCurProgramsDate(context.getString(R.string.pref_program_date_key));
        return ProgramFragment.newInstance(channelId, format.format(savedDate));
    }

    @Override
    public int getCount() {
        return tvChannels.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tvChannels.get(position).getName();
    }

    public String getImagePath(int position) {
        return tvChannels.get(position).getPicture();
    }

}
