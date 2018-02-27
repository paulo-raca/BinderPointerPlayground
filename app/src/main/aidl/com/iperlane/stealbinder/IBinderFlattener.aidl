// IEcho.aidl
package com.iperlane.stealbinder;

import com.iperlane.stealbinder.FlatBinderObject;
import android.content.Intent;

interface IBinderFlattener {
    FlatBinderObject flattenBinder(IBinder binder);
    FlatBinderObject flattenService(in Intent intent);
}
