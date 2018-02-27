package com.iperlane.stealbinder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class EchoService extends Service {
    public static final String TAG = EchoService.class.getSimpleName();

    IEcho echo = new IEcho.Stub() {
        @Override
        public String echo(String str) throws RemoteException {
            Log.i(TAG, "Echoing: '" + str + "'");
            return str;
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return echo.asBinder();
    }
}
