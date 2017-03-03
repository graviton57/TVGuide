package com.havrylyuk.tvapp.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.havrylyuk.tvapp.R;
import com.havrylyuk.tvapp.fragment.ChannelFragment;
import com.havrylyuk.tvapp.fragment.ProgramFragment;
import com.havrylyuk.tvapp.util.PreferencesHelper;

import java.text.SimpleDateFormat;
import java.util.Locale;


/**
 *
 * Created by Igor Havrylyuk on 20.02.2017.
 */

public class ChannelsViewPagerAdapter extends FragmentStatePagerAdapter {

    private Cursor tvChannels;
    private Context context;
    private PreferencesHelper preferencesHelper;
    private final SimpleDateFormat format;

    public ChannelsViewPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
        preferencesHelper = PreferencesHelper.getInstance();
        format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    public void swapCursor( Cursor cursor) {
        this.tvChannels = cursor;
    }

    @Override
    public Fragment getItem(int position) {
        tvChannels.moveToPosition(position);
        long channelId = tvChannels.getLong(ChannelFragment.COL_CH_ID);
        long savedDate = preferencesHelper.getCurProgramsDate(context.getString(R.string.pref_program_date_key));
        return ProgramFragment.newInstance(channelId, format.format(savedDate));
    }

    @Override
    public int getCount() {
        if (tvChannels != null) {
            return tvChannels.getCount();
        }
        return 0;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        tvChannels.moveToPosition(position);
        return tvChannels.getString(ChannelFragment.COL_NAME);
    }

    public String getImagePath(int position) {
        tvChannels.moveToPosition(position);
        return tvChannels.getString(ChannelFragment.COL_IMAGE);
    }

}
