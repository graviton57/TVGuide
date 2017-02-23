package com.havrylyuk.tvapp.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


/**
 *
 * Created by Igor Havrylyuk on 18.02.2017.
 */

public class TvGuideSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static TvGuideSyncAdapter sTvGuideSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sTvGuideSyncAdapter == null) {
                sTvGuideSyncAdapter = new TvGuideSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sTvGuideSyncAdapter.getSyncAdapterBinder();
    }
}