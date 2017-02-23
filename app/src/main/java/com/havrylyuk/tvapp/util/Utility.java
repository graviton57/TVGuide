package com.havrylyuk.tvapp.util;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import com.havrylyuk.tvapp.R;
import com.havrylyuk.tvapp.data.local.TvContract;
import com.havrylyuk.tvapp.sync.TvGuideSyncAdapter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * Created by Igor Havrylyuk on 18.02.2017.
 */

public class Utility {


    public static boolean isNetworkAvailable(final Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressWarnings("deprecation")
        boolean isNetworkAvailable = cm.getBackgroundDataSetting() &&
                cm.getActiveNetworkInfo() != null;
        return isNetworkAvailable;
    }

    // check internet access now!
    public static boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
        return false;
    }


    public  static int getChannelSortValues(boolean isDesc) {
        return isDesc ? R.array.channel_sort_values_desc : R.array.channel_sort_values;
    }

    public static String getPrefSortChannelOrder(String prefOrder ) {
        if ("none".equalsIgnoreCase(prefOrder)) {
            return  null;
        } else if ("category.desc".equalsIgnoreCase(prefOrder)) {
            return TvContract.ChannelEntry.TABLE_NAME
                    + "." + TvContract.ChannelEntry.COLUMN_CHANNEL_CATEGORY_ID + " DESC";
        } else if ("category".equalsIgnoreCase(prefOrder)) {
            return TvContract.ChannelEntry.TABLE_NAME + "."
                    + TvContract.ChannelEntry.COLUMN_CHANNEL_CATEGORY_ID;
        }  else if ("name.desc".equalsIgnoreCase(prefOrder)) {
            return  TvContract.ChannelEntry.TABLE_NAME + "."
                    +TvContract.ChannelEntry.COLUMN_CHANNEL_NAME + " DESC";
        }  else return  TvContract.ChannelEntry.TABLE_NAME + "."
                +TvContract.ChannelEntry.COLUMN_CHANNEL_NAME ; //default sort by name
    }

    public static long getLastSyncTime(Context context) {
        long result = 0;
        try {
            Method getSyncStatus =
                    ContentResolver.class.getMethod("getSyncStatus", Account.class, String.class);
            Account mAccount = TvGuideSyncAdapter.getSyncAccount(context);
            if (mAccount != null ) {
                Object status = getSyncStatus.invoke(null, mAccount, context.getResources().getString(R.string.content_authority));
                Class<?> statusClass = Class.forName("android.content.SyncStatusInfo");
                boolean isStatusObject = statusClass.isInstance(status);
                if (isStatusObject) {
                    Field successTime = statusClass.getField("lastSuccessTime");
                    result = successTime.getLong(status);
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException |
                  IllegalArgumentException | ClassNotFoundException |
                  NoSuchFieldException | NullPointerException e) {
                // ignore it
        } catch (InvocationTargetException e) {
            Log.e("Utility", e.getMessage() + e.getCause().getMessage());
        }
        return result;
    }

    public static boolean isNotDuplicateSync(Context context) {
        long lastSyncTime = PreferencesHelper.getInstance()
                .getLastSyncTime(context.getString(R.string.pref_last_sync_time_key));
        /*boolean isFirstRun = PreferencesHelper.getInstance()
                .getFirstRun(context.getString(R.string.pref_fist_run_key));*/
        return  (System.currentTimeMillis() - lastSyncTime) / 1000 > TvGuideSyncAdapter.RUN_NEXT_SYNC_DELAY;
    }
}
