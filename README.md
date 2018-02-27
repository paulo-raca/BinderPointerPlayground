# Binder Playground
The idea behind this project is understanding how binders are passed between processes, and whenever we could "steal" them from other processes.

## Parcel
Surprisingly, most of the magic doesn't happen in the Binder-related code, but when creating and reading parcels.

On a high-level view, a Parcel seems like an ordinary serialization protocol -- every object written is transformed into a bunch of bytes and appended at the end of a blob. So, how come Parcels can also send "magic" bits like Binder handles and File descriptors?

Turns out that it performs an ordinary serialization of the these objects: It simply stores the handles, pointers and file descriptors on the blob. **And**, most importantly, it also stores a list of the offsets where magic bits are located inside the blob.

When a parcel is transfered in a binder transaction, the kernel looks up the magic offsets and translates file descriptors and binder handles to be usable on the new process.

Check `writeStrongBinder`, `flatten_binder` and `Parcel::writeObject` in [Parcel.cpp](https://android.googlesource.com/platform/frameworks/native/+/master/libs/binder/Parcel.cpp)

The actual data written on the Parcel's blob is an `struct flat_binder_object`, as defined into Linux's [binder.h](https://github.com/torvalds/linux/blob/master/include/uapi/linux/android/binder.h)

## Hacking
Since the binder pointers and handles are stored flat on a Parcel, Instead of going into the low-level implementation of Binder, the easiest way to access them is using the Parcel APIs: Write a binder, read a few integers.

Likewise, it is possible to create a Binder instance from an arbitrary handle: Write a binder, change the handle, read back a binder.

### Stealing Binders
My initial hipotesis was that, knowing a binder handle that exists in process A, I would be able to duplicate the handle on process B and it would communicate with the original binder.

Turns out that, like file descriptors, binder handles are small-integers that are only valid within the scope of a process. When a binder is sent to another process, it is assigned a new handle number.

So, no, you cannot steal a binder this way.

### Enumerating Binders
As mentioned, binder handles are assigned incrementing integers.

While I couln't steal a binder from a different process, I can just count 1 to N to get all binders accessible to my proccess: 
```
Binder #1: android.app.IActivityManager
Binder #2: android.content.pm.IPackageManager
Binder #3: android.app.IAlarmManager
Binder #4: android.view.IWindowManager
Binder #5: android.view.IApplicationToken
Binder #6: android.hardware.display.IDisplayManager
Binder #7: android.net.IConnectivityManager
Binder #8: android.content.IContentProvider
Binder #10: android.app.IUiModeManager
Binder #11: android.view.accessibility.IAccessibilityManager
Binder #12: com.android.internal.view.IInputMethodManager
Binder #13: android.view.IWindowSession
Binder #14: android.ui.ISurfaceComposer
Binder #15: android.gui.DisplayEventConnection
Binder #16: android.gui.DisplayEventConnection
Binder #17: com.android.internal.view.IInputMethodSession
Binder #18: android.gui.DisplayEventConnection
Binder #19: android.view.IGraphicsStats
Binder #20: android.gui.IGraphicBufferProducer
Binder #21: android.hardware.input.IInputManager
Binder #22: android.media.IAudioService
```
