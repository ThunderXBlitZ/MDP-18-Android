package com.example.mdp_android;

import android.bluetooth.BluetoothAdapter;
import android.content.res.Resources;

public class Util {
    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }
}
