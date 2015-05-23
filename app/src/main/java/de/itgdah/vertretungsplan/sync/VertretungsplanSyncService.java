package de.itgdah.vertretungsplan.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Stub sync service.
 */
public class VertretungsplanSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static VertretungsplanSyncAdapter sVertretungsplanSyncAdapter = null;

    private static final String LOG_TAG = VertretungsplanSyncService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate - VertretungsplanSyncService");
        synchronized (sSyncAdapterLock) {
            if (sVertretungsplanSyncAdapter == null) {
                sVertretungsplanSyncAdapter = new VertretungsplanSyncAdapter
                        (getApplicationContext(), true);
            }
        }
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sVertretungsplanSyncAdapter.getSyncAdapterBinder();
    }
}
