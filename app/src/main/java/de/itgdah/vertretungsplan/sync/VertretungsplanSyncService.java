package de.itgdah.vertretungsplan.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Moritz on 5/18/2015.
 */
public class VertretungsplanSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static VertretungsplanSyncAdapter sVertretungsplanSyncAdapter = null;

    private static final String LOG_TAG = VertretungsplanSyncService.class.getSimpleName();

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate - VertretungsplanSyncService");
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapterLock == null) {
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
