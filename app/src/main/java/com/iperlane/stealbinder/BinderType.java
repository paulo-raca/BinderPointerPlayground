package com.iperlane.stealbinder;

import java.util.Arrays;

public enum BinderType {
    BINDER     (false, false),
    WEAK_BINDER(false, true),
    HANDLE     (true, false),
    WEAK_HANDLE(true, true);

    private final boolean weak;
    private final boolean remote;
    private final byte[] type;
    private final int typeInt;

    BinderType(boolean isRemote, boolean isWeak) {
        this.remote = isRemote;
        this.weak = isWeak;
        this.type = new byte[] {
                (byte)(isWeak ? 'w' : 's'),
                (byte)(isRemote ? 'h' : 'b'),
                (byte)('*'),
                (byte)(0x85)};
        this.typeInt = ((type[0] & 0xFF) << 24) | ((type[1] & 0xFF) << 16) | ((type[2] & 0xFF) << 8) | ((type[3] & 0xFF) << 0);
    }

    public boolean isRemote() {
        return this.remote;
    }
    public boolean isLocal() {
        return !this.isRemote();
    }
    public boolean isWeak() {
        return this.weak;
    }
    public boolean isStrong() {
        return !this.isWeak();
    }
    public byte[] getTypeHeader() {
        return type;
    }
    public int getTypeHeaderInt() {
        return typeInt;
    }

    public static BinderType get(int typeHeaderInt) {
        for (BinderType t : values()) {
            if (t.getTypeHeaderInt() == typeHeaderInt)
                return t;
        }
        return null;
    }
    public static BinderType get(byte[] typeHeader) {
        for (BinderType t : values()) {
            if (Arrays.equals(t.getTypeHeader(), typeHeader))
                return t;
        }
        return null;
    }
}