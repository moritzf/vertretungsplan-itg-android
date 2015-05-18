package de.itgdah.vertretungsplan.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Moritz on 5/18/2015.
 */
public class VertretungsplanAuthenticatorService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
