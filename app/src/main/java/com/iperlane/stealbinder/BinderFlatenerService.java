package com.iperlane.stealbinder;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.concurrent.Semaphore;

public class BinderFlatenerService extends Service {
    public static final String TAG = BinderFlatenerService.class.getSimpleName();

    IBinderFlattener flattener = new IBinderFlattener.Stub() {
        @Override
        public FlatBinderObject flattenBinder(IBinder binder) throws RemoteException {
            FlatBinderObject ret = new FlatBinderObject(binder);
            Log.i(TAG, "flattenBinder(" + binder + "): " + ret);
            return ret;
        }

        @Override
        public FlatBinderObject flattenService(Intent intent) throws RemoteException {
            final Semaphore sem = new Semaphore(0);
            final IBinder buf[] = {null};

            ServiceConnection mConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName className, IBinder service) {
                    buf[0] = service;
                    sem.release();
                }

                @Override
                public void onServiceDisconnected(ComponentName arg0) {}
            };
            if (!bindService(intent, mConnection, Context.BIND_AUTO_CREATE)) {
                throw new RemoteException("Failed to bind");
            }

            try {
                sem.acquire();
            } catch (InterruptedException e) {
                throw new RemoteException("Interrupted");
            }
            FlatBinderObject ret = new FlatBinderObject(buf[0]);
            Log.i(TAG, "flattenerService(" + intent + "): " + ret);
            return ret;
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return flattener.asBinder();
    }
}
