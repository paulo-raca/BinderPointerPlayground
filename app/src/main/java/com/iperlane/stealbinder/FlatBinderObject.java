package com.iperlane.stealbinder;

import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.Arrays;

/**
 * This class extracts a Binder's serialized structure, as defined on `struct flat_binder_object` on binder.h.
 * This allows me to get the raw pointers and handles associated with a binder.
 */
class FlatBinderObject implements Parcelable {
    final String TAG = "FlatBinderObject";

    static boolean isPtr64;
    static {
        Parcel p = Parcel.obtain();
        try {
            p.writeStrongBinder(new Binder());
            if (p.dataSize() == 24) {
                isPtr64 = true;
            } else if (p.dataSize() == 16) {
                isPtr64 = false;
            } else {
                throw new Error("Invalid size for flat_binder_object: " + p.dataSize());
            }
        } finally {
            p.recycle();
        }
    }

    BinderType binderType;
    int flags;
    long handleOrBinder;
    long cookie;


    private static Parcel binderToParcel(IBinder binder) {
        Parcel p = Parcel.obtain();
        p.writeStrongBinder(binder);
        p.setDataPosition(0);
        return p;
    }

    public FlatBinderObject(IBinder binder) {
        this(binderToParcel(binder));
    }

    public FlatBinderObject(Parcel p) {
        int headerInt = p.readInt();
        this.binderType = BinderType.get(headerInt);

        if (this.binderType == null) {
            throw new RuntimeException("Unknown flat_binder_object.type: " + headerInt);
        }

        this.flags = p.readInt();

        if (this.binderType.isRemote()) {
            this.handleOrBinder = p.readInt();
            if (isPtr64)
                p.readInt();  // Skip extra bytes from binder union
        } else {
            this.handleOrBinder = isPtr64 ? p.readLong() : p.readInt();
        }

        this.cookie = isPtr64 ? p.readLong() : p.readInt();
    }

    public IBinder getBinder() {
        Parcel p = Parcel.obtain();
        p.writeStrongBinder(new Binder());
        p.setDataPosition(0);
        this.writeToParcel(p, 0);
        p.setDataPosition(0);
        return p.readStrongBinder();
    }

    @Override
    public String toString() {
        if (binderType.isLocal()) {
            return String.format("FlatBinderObject(binderType=%s, flags=0x%04x, binder=0x%016x, cookie=0x%016x)", binderType, flags, handleOrBinder, cookie);
        } else {
            return String.format("FlatBinderObject(binderType=%s, flags=0x%04x, handle=%d)", binderType, flags, handleOrBinder);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.binderType.getTypeHeaderInt());
        dest.writeInt(this.flags);

        if (this.binderType.isRemote()) {
            dest.writeInt((int) this.handleOrBinder);
            if (isPtr64)
                dest.writeInt(0);  // Skip extra bytes from binder union
        } else {
            if (isPtr64) {
                dest.writeLong(this.handleOrBinder);
            } else {
                dest.writeInt((int)this.handleOrBinder);
            }
        }
        if (isPtr64) {
            dest.writeLong(this.cookie);
        } else {
            dest.writeInt((int)this.cookie);
        }
    }

    public static final Parcelable.Creator<FlatBinderObject> CREATOR = new Parcelable.Creator<FlatBinderObject>() {
        public FlatBinderObject createFromParcel(Parcel in) {
            return new FlatBinderObject(in);
        }

        public FlatBinderObject[] newArray(int size) {
            return new FlatBinderObject[size];
        }
    };
}