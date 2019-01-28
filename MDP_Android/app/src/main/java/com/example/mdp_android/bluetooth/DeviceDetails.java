package com.example.mdp_android.bluetooth;

/**
 * Created by ongji on 3 Feb 2018.
 */

public class DeviceDetails {

    private String deviceName;
    private String address;
    private boolean connected = false;

    public String getDeviceName() {
        return deviceName != null? deviceName : "Unknown Device";
    }

    public boolean getConnected() {
        return connected;
    }

    public void setConnected(Boolean value) {
        connected = value;
    }

    public String getAddress() {
        return address;
    }

    public DeviceDetails(String name, String address, Boolean connected){
        this.deviceName = name;
        this.address = address;
        this.connected = connected;
    }

    public String toString(){
        return deviceName == null? "Unknown Device" : deviceName;
    }
}
