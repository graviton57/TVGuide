package com.havrylyuk.tvapp.util;

import android.content.SharedPreferences;

import com.havrylyuk.tvapp.TvApp;
import com.havrylyuk.tvapp.sync.TvGuideSyncAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 *
 * Created by Igor Havrylyuk on 18.02.2017.
 */


public class PreferencesHelper {

    private static PreferencesHelper sInstance = null;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public static PreferencesHelper getInstance() {
        if(sInstance == null) {
            sInstance = new PreferencesHelper();
        }
        return sInstance;
    }

    public PreferencesHelper() {
        this.sharedPreferences = TvApp.getSharedPreferences();
        this.editor = this.sharedPreferences.edit();
    }

    //for save data in SharedPreferences
    public void setScheduleDaysCount(String name, int daysCount){
        editor.putInt(name, daysCount);
        editor.apply();
    }

    //for the loading of data from SharedPreferences
    public String getScheduleDaysCount(String prefName){
        return sharedPreferences.getString(prefName, "1");
    }

    public Boolean showDisplayNotifications(String prefName){
        return sharedPreferences.getBoolean(prefName, true);
    }

    public void setChannelSortType(String name, String value){
        editor.putString(name, value);
        editor.apply();
    }

    public String getChannelSortType(String prefName){
        return sharedPreferences.getString(prefName, "name");
    }

    public void setCurProgramsDate(String name, long value){
        editor.putLong(name, value);
        editor.apply();
    }

    public long getCurProgramsDate(String prefName){
        return sharedPreferences.getLong(prefName, new Date().getTime());
    }

    public void setTvServerStatus(String name, int status){
        editor.putInt(name, status);
        editor.apply();
    }

    public int getTvServerStatus(String prefName){
        return sharedPreferences.getInt(prefName, TvGuideSyncAdapter.SERVER_STATUS_UNKNOWN);
    }

    public void setLastSyncTime(String name, long lastSyncTime){
        editor.putLong(name, lastSyncTime);
        editor.apply();
    }

    public long getLastSyncTime(String prefName){
        return sharedPreferences.getLong(prefName, 0);
    }

    public long getSyncInterval(String prefName){
        return sharedPreferences.getLong(prefName, 86400);
    }

    public boolean getFirstRun(String prefName){
        return sharedPreferences.getBoolean(prefName, true);
    }

    public void setFirstRun(String prefName, boolean value){
        editor.putBoolean(prefName, value);
        editor.apply();
    }
}
