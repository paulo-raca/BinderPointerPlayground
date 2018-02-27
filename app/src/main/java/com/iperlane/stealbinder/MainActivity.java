package com.iperlane.stealbinder;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    IEcho echoService;
    IBinderFlattener flattenerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupEchoService();
        setupFlattenService();

        Button btnEcho = (Button) findViewById(R.id.btn_echo);
        btnEcho.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IEcho echo = echoService;
                Log.i(TAG, "IEcho = " + echo.asBinder());
                String message;

                if (echo != null) {
                    try {
                        message = echo.echo("Echo.. echo.. echo.. echo...");
                    } catch (Exception e) {
                        message = "Failed to call service: " + e.toString();
                    }
                } else {
                    message = "Service not connected 😞";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(message);
                builder.create().show();
            }
        });

        Button btnFlatten = (Button) findViewById(R.id.btn_flatten_binder);
        btnFlatten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.i(TAG, "local binder: " + new FlatBinderObject(new android.os.Binder()));

                    FlatBinderObject flatBinderObject = new FlatBinderObject(echoService.asBinder());
                    Log.i(TAG, "echoService: "+flatBinderObject);

                    Parcel p = Parcel.obtain();
                    flatBinderObject.writeToParcel(p, 0);
                    p.setDataPosition(0);
                    FlatBinderObject flatBinderObjectCopy = new FlatBinderObject(p);
                    Log.i(TAG, "echoService (local copy): "+flatBinderObjectCopy);

                    FlatBinderObject remoteCopy = flattenerService.flattenBinder(echoService.asBinder());
                    Log.i(TAG, "echoService (remote binder): "+remoteCopy);

                    FlatBinderObject remoteGetService = flattenerService.flattenService(new Intent(MainActivity.this, EchoService.class));
                    Log.i(TAG, "bindService (remote bindService): "+remoteCopy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Button btnListBinders = (Button) findViewById(R.id.btn_list_binders);
        btnListBinders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlatBinderObject flatBinderObject = new FlatBinderObject(echoService.asBinder());
                for (int i=1; i<25; i++) {
                    flatBinderObject.handleOrBinder = i;

                    try {
                        String name = flatBinderObject.getBinder().getInterfaceDescriptor();
                        if (!"".equals(name)) {
                            Log.i(TAG, "Binder #" + flatBinderObject.handleOrBinder + ": " + name);
                        }
                    } catch (Exception e) {
                    }
                }
            }
        });
    }


    protected void setupEchoService() {
        ServiceConnection mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                Log.i(TAG, "Echo service connected");
                echoService = IEcho.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                Log.i(TAG, "Echo service disconnected");
                echoService = null;
            }
        };
        Intent intent = new Intent(MainActivity.this, EchoService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    protected void setupFlattenService() {
        ServiceConnection mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                Log.i(TAG, "Flatten service connected");
                flattenerService = IBinderFlattener.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                Log.i(TAG, "Flatten service disconnected");
                flattenerService = null;
            }
        };
        Intent intent = new Intent(MainActivity.this, BinderFlatenerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
}
