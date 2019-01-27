package com.example.mdp_android;

/**
 * Created by ongji on 3 Feb 2018.
 */

public class DeviceDetails {

    private String deviceName;
    private String address;
    private boolean connected;

    public String getDeviceName() {
        return deviceName;
    }

    public boolean getConnected() {
        return connected;
    }

    public String getAddress() {
        return address;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public DeviceDetails(String name, String address, String connected){
        this.deviceName = name;
        this.address = address;
        this.connected = connected == "true";
    }

    public String toString(){
        return deviceName == null? "null" : deviceName;
    }
}
