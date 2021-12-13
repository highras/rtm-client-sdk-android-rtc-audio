package com.livedata.rtc;

import android.content.Context;

public enum RTCEngine {
    INSTANCE;

    // Load native library
    static {
        System.loadLibrary("rtcEngineNative");
    }
    static int sessionID = -1;

    // Native methods
//    public static native String create(Object object, int osversion,boolean stereo, Object view);
    public static native String create(Object object, String rtcEndpoint, int osversion, boolean stereo, long pid, long uid,
                                       Context mcontext);

    public static native void switchVoiceOutput(boolean useSpeaker);
    public static native void canSpeak(boolean flag);
    public static native void setMicphoneGain(int level);
    public static native void delete();

    public static native byte[] enterRTCRoom(String token, long rid);
    public static native void leaveRTCRoom(long rid);
    public static native void setBackground(boolean flag);
    public static native void RTCClear();
    public static native void headsetStat();
    public static native String setActivityRoom(long rid);
    public static native String setVoiceStat(boolean flag);
    public static native void setdiscardable(boolean flag);
}
