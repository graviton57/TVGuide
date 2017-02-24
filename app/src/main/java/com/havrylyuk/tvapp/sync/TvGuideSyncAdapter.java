package com.havrylyuk.tvapp.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.havrylyuk.tvapp.BuildConfig;
import com.havrylyuk.tvapp.R;
import com.havrylyuk.tvapp.activity.MainActivity;
import com.havrylyuk.tvapp.data.local.TvContract.CategoryEntry;
import com.havrylyuk.tvapp.data.local.TvContract.ChannelEntry;
import com.havrylyuk.tvapp.data.local.TvContract.ProgramEntry;
import com.havrylyuk.tvapp.data.remote.TvApiClient;
import com.havrylyuk.tvapp.data.remote.TvService;
import com.havrylyuk.tvapp.model.TvCategory;
import com.havrylyuk.tvapp.model.TvChannel;
import com.havrylyuk.tvapp.model.TvProgram;
import com.havrylyuk.tvapp.util.PreferencesHelper;
import com.havrylyuk.tvapp.util.Utility;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


/**
 *
 * Created by Igor Havrylyuk on 18.02.2017.
 */

public class TvGuideSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final long FLEX_DIVIDER = 10;
    public static final int RUN_NEXT_SYNC_DELAY = 30;//seconds
    public static final String EXTRA_KEY_SYNC = "com.havrylyuk.tvapp.sync.EXTRA_KEY_SYNC";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SERVER_STATUS_OK, SERVER_STATUS_DOWN, STATUS_SERVER_NOT_FOUND,
            STATUS_SERVER_BAD_REQ, SERVER_STATUS_UNKNOWN,SERVER_STATUS_ERROR})
    public @interface TvServerStatus {}
    public static final int SERVER_STATUS_OK = 0;
    public static final int SERVER_STATUS_DOWN = 1;
    public static final int STATUS_SERVER_NOT_FOUND = 2;
    public static final int STATUS_SERVER_BAD_REQ = 3;
    public static final int SERVER_STATUS_UNKNOWN = 4;
    public static final int SERVER_STATUS_ERROR = 5;

    private final static String LOG_TAG = TvGuideSyncAdapter.class.getSimpleName();
    private PreferencesHelper preferencesHelper;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int TV_GUIDE_NOTIFICATION_ID = 3004;

    public TvGuideSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        preferencesHelper = PreferencesHelper.getInstance();
    }

    public TvGuideSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        if ( Utility.isNotDuplicateSync(getContext())) {
            if (BuildConfig.DEBUG) Log.d(LOG_TAG,"Start sync!");
            sendSyncStatus(START_SYNC);
            final TvService service = TvApiClient.getClient().create(TvService.class);
            syncCategories(service, provider, syncResult);
            syncChannels(service, provider, syncResult);
            syncPrograms(service, provider, syncResult);
            notifyTvGuide(syncResult.stats.numInserts, syncResult.stats.numIoExceptions);
            preferencesHelper.setLastSyncTime(getContext().getString(R.string.pref_last_sync_time_key),
                                                System.currentTimeMillis());
            preferencesHelper.setFirstRun(getContext().getString(R.string.pref_fist_run_key),false);
            sendSyncStatus(END_SYNC);
            if (BuildConfig.DEBUG) Log.d(LOG_TAG,"End sync!");
        }
    }

    private void syncCategories(TvService service, ContentProviderClient provider, SyncResult syncResult) {
        try {
            Call<List<TvCategory>> responseCall = service.getCategories();
            Response<List<TvCategory>> response = responseCall.execute();
            if (response.isSuccessful()){
                List<TvCategory> categories = response.body();
                if (categories != null) {
                    ContentValues[] values = new ContentValues[categories.size()];
                    for (int i = 0; i < categories.size(); i++) {
                        values[i] = categoryToContentValues(categories.get(i));
                    }
                    syncResult.stats.numInserts =+ provider.bulkInsert(CategoryEntry.CONTENT_URI, values );
                    setTvServerStatus(SERVER_STATUS_OK);
                } else  {
                    Log.e(LOG_TAG, "Error:empty category response body");
                    ++syncResult.stats.numIoExceptions;
                    }
                } else {
                    genericError(response.code());
                    ++syncResult.stats.numIoExceptions;
                }
        } catch (RemoteException e) {
            setTvServerStatus(SERVER_STATUS_ERROR);
            Log.e(LOG_TAG, e.toString());
            ++syncResult.stats.numIoExceptions;
        } catch (IOException e) {
            setTvServerStatus(SERVER_STATUS_DOWN);
            ++syncResult.stats.numIoExceptions;
            Log.e(LOG_TAG, e.toString());
        }
    }

    private ContentValues categoryToContentValues(TvCategory category) {
        ContentValues result =  new ContentValues();
        result.put(CategoryEntry.COLUMN_CATEGORY_ID,category.getId());
        result.put(CategoryEntry.COLUMN_CATEGORY_TITLE,category.getTitle());
        result.put(CategoryEntry.COLUMN_CATEGORY_PICTURE,category.getPicture());
        return result;
    }

    private void syncChannels(TvService service, ContentProviderClient provider, SyncResult syncResult) {
        try {
            Call<List<TvChannel>> responseCall = service.getChanels();
            Response<List<TvChannel>> response = responseCall.execute();
            if (response.isSuccessful()){
                List<TvChannel> tvChannels = response.body();
                if (tvChannels != null) {
                    ContentValues[] values = new ContentValues[tvChannels.size()];
                    for (int i = 0; i < tvChannels.size(); i++) {
                        values[i] = channelToContentValues(tvChannels.get(i));
                    }
                    syncResult.stats.numInserts =+ provider.bulkInsert(ChannelEntry.CONTENT_URI, values );
                    setTvServerStatus(SERVER_STATUS_OK);
                } else  {
                    Log.e(LOG_TAG, "Error:empty channel response body");
                    ++syncResult.stats.numIoExceptions;
                    }
                } else {
                    genericError(response.code());
                    ++syncResult.stats.numIoExceptions;
                }
        } catch (RemoteException e) {
            Log.e(LOG_TAG, e.toString());
            setTvServerStatus(SERVER_STATUS_ERROR);
            ++syncResult.stats.numIoExceptions;
        } catch (IOException e) {
            setTvServerStatus(SERVER_STATUS_DOWN);
            ++syncResult.stats.numIoExceptions;
            Log.e(LOG_TAG, e.toString());
        }
    }

    private ContentValues channelToContentValues(TvChannel tvChannel) {
        ContentValues result =  new ContentValues();
        result.put(ChannelEntry.COLUMN_CHANNEL_ID, tvChannel.getId());
        result.put(ChannelEntry.COLUMN_CHANNEL_NAME, tvChannel.getName());
        result.put(ChannelEntry.COLUMN_CHANNEL_PICTURE, tvChannel.getPicture());
        result.put(ChannelEntry.COLUMN_CHANNEL_CATEGORY_ID, tvChannel.getCategoryId());
        result.put(ChannelEntry.COLUMN_CHANNEL_URL, tvChannel.getUrl());
        return result;
    }

    private void syncPrograms(TvService service, ContentProviderClient provider, SyncResult syncResult) {
        try {
            int countDays = Integer.parseInt(preferencesHelper.getScheduleDaysCount(getContext().getString(R.string.pref_schedule_days_count_key)));
            getContext().getContentResolver().delete(ProgramEntry.CONTENT_URI, null, null);
                for (int d = 0; d < countDays; d++) {
                    Call<List<TvProgram>> responseCall = service.getPrograms(System.currentTimeMillis() + DAY_IN_MILLIS * d);
                    Response<List<TvProgram>> response = responseCall.execute();
                    if (response.isSuccessful()){
                        List<TvProgram> tvPrograms = response.body();
                        if (tvPrograms != null) {
                            ContentValues[] values = new ContentValues[tvPrograms.size()];
                            for (int i = 0; i < tvPrograms.size(); i++) {
                                values[i] = programToContentValues(tvPrograms.get(i));
                            }
                            syncResult.stats.numInserts =+ provider.bulkInsert(ProgramEntry.CONTENT_URI, values );
                            setTvServerStatus(SERVER_STATUS_OK);
                        } else  {
                            Log.e(LOG_TAG, "Error: empty ic_menu_program response body");
                            ++syncResult.stats.numIoExceptions;
                        }
                    } else {
                        genericError(response.code());
                        ++syncResult.stats.numIoExceptions;
                    }
                }
        } catch (RemoteException e) {
            Log.e(LOG_TAG, e.toString());
            setTvServerStatus(SERVER_STATUS_ERROR);
            ++syncResult.stats.numIoExceptions;
        } catch (IOException e) {
            setTvServerStatus(SERVER_STATUS_DOWN);
            ++syncResult.stats.numIoExceptions;
            Log.e(LOG_TAG, e.toString());
        }
    }

    private ContentValues programToContentValues(TvProgram tvProgram) {
        ContentValues result =  new ContentValues();
        result.put(ProgramEntry.COLUMN_PROGRAM_CHANEL_ID, tvProgram.getChannelId());
        result.put(ProgramEntry.COLUMN_PROGRAM_DATE, tvProgram.getDate());
        result.put(ProgramEntry.COLUMN_PROGRAM_TIME, tvProgram.getTime());
        result.put(ProgramEntry.COLUMN_PROGRAM_TITLE, tvProgram.getTitle());
        result.put(ProgramEntry.COLUMN_PROGRAM_DESCRIPTION, tvProgram.getDescription());
        return result;
    }

    private void genericError(int errorCode) {
        switch (errorCode) {
            case 400:
                setTvServerStatus(STATUS_SERVER_BAD_REQ);
                Log.e(LOG_TAG, "Error 400, Bad Request");
                break;
            case 404:
                setTvServerStatus(STATUS_SERVER_NOT_FOUND);
                Log.e(LOG_TAG, "Error 404, Not Found");
                break;
            default:
                setTvServerStatus(SERVER_STATUS_ERROR);
                Log.e(LOG_TAG, "Error "+errorCode+", Generic Error");
        }
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, long syncInterval, long flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        // Create the account desc and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));
        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {
         /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        final long interval =  PreferencesHelper.getInstance()
                .getSyncInterval(context.getString(R.string.pref_auto_sync_interval_key));
        //Since we've created an account
        TvGuideSyncAdapter.configurePeriodicSync(context, interval, interval / FLEX_DIVIDER);
        //Without calling setSyncAutomatically, our periodic sync will not be enabled.
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        //Finally, let's do a sync to get things started
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    private void notifyTvGuide(long updatedCount, long errorCount) {
        Context context = getContext();
        boolean displayNotifications = preferencesHelper.showDisplayNotifications(
                context.getString(R.string.pref_enable_notifications_key));
        if ( displayNotifications ) {
            String contentText = getNotifyServerStatusMessage(updatedCount, errorCount);
            Resources resources = context.getResources();
            int iconId = R.mipmap.ic_launcher;
            int artResourceId = R.mipmap.ic_launcher;
            Bitmap largeIcon = BitmapFactory.decodeResource(resources, artResourceId);
                    String title = context.getString(R.string.app_name);
                    NotificationCompat.Builder mBuilder =
                            (NotificationCompat.Builder) new NotificationCompat.Builder(getContext())
                                    .setColor(ContextCompat.getColor(context,R.color.colorPrimaryLight))
                                    .setSmallIcon(iconId)
                                    .setLargeIcon(largeIcon)
                                    .setAutoCancel(true)
                                    .setContentTitle(title)
                                    .setContentText(contentText);
                    Intent resultIntent = new Intent(context, MainActivity.class);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent( 0,
                                    PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(resultPendingIntent);
                    NotificationManager mNotificationManager =
                            (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(TV_GUIDE_NOTIFICATION_ID, mBuilder.build());
                }
        }

     private void setTvServerStatus(@TvServerStatus int serverStatus){
         preferencesHelper.setTvServerStatus(
                 getContext().getString(R.string.pref_server_status_key),
                 serverStatus);
    }

    @SuppressLint("SwitchIntDef")
    private String getNotifyServerStatusMessage(long updatedCount, long errorCount) {
        String message = getContext().getString(R.string.satus_empty_list);
        @TvGuideSyncAdapter.TvServerStatus int status =
                preferencesHelper.getTvServerStatus(getContext().getString(R.string.pref_server_status_key));
        switch (status) {
            case TvGuideSyncAdapter.SERVER_STATUS_OK:
                message = getContext().getString(R.string.notify_sync_success, updatedCount);
                break;
            case TvGuideSyncAdapter.SERVER_STATUS_DOWN:
                message = getContext().getString(R.string.satus_empty_list_server_down);
                break;
            case TvGuideSyncAdapter.SERVER_STATUS_ERROR:
                message = getContext().getString(R.string.satus_empty_list_server_error, errorCount);
                break;
            case TvGuideSyncAdapter.STATUS_SERVER_NOT_FOUND:
                message = getContext().getString(R.string.satus_empty_list_bad_request);
                break;
            default:
                if (!Utility.isNetworkAvailable(getContext())) {
                    message = getContext().getString(R.string.satus_empty_list_no_network);
                }
        }
        return message;
    }

    private static final int START_SYNC = 0;
    private static final int END_SYNC = 1;

    // send sync status
    private void sendSyncStatus(int status) {
        Intent intentUpdate = new Intent();
        intentUpdate.setAction(MainActivity.SyncContentReceiver.SYNC_RESPONSE_STATUS);
        intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
        intentUpdate.putExtra(EXTRA_KEY_SYNC, status);
        getContext().sendBroadcast(intentUpdate);
    }
}
