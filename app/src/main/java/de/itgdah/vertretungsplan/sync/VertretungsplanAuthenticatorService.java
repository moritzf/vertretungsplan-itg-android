package de.itgdah.vertretungsplan.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Moritz on 5/18/2015.
 */
public class VertretungsplanAuthenticatorService extends Service {

    private VertretungsplanAuthenticator  mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new VertretungsplanAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
