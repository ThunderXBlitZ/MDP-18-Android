package com.example.mdp_android.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.mdp_android.MainActivity;
import com.example.mdp_android.Util;

/**
 * High-level Class that works on top of BlueChatService
 */
public class BluetoothManager {
    private static BluetoothAdapter mBluetoothAdapter;
    private static BluetoothChatService mChatService;
    private static BluetoothManager _instance;
    private Handler _mHandler;
    private Activity mActivity;
    public static final int BT_REQUEST_CODE = 0; // code for detecting whether bluetooth is switched on

    private final String LAST_DEVICE = "storedRecord";
    private final String LAST_DEVICE_NAME = "storedName";
    private final String LAST_DEVICE_ADDRESS = "storedAddress";

    public BluetoothManager(Activity activity, Handler mHandler){
        mActivity = activity;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mChatService = new BluetoothChatService(activity, nHandler);
        _mHandler = mHandler;
        _instance = this;
    }

    public static BluetoothManager getInstance(){
        return _instance;
    }

    /**
     * Detects if bluetooth is on, else prompt user to switch on (return false)
     * @return true if bluetooth is on
     */
    public Boolean setupBluetooth(){
        if (mBluetoothAdapter == null) {
            Toast.makeText(mActivity, "Bluetooth not supported on this device!",Toast.LENGTH_SHORT).show();
            return false;
        }
        else{
            if(!mBluetoothAdapter.isEnabled()){
                Toast.makeText(mActivity, "Please enable Bluetooth and device location!",Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }
    }

    // discover nearby bluetooth devices
    public void listBluetoothDevices(){
        if(setupBluetooth()){
            if (mBluetoothAdapter.isDiscovering()){
                mBluetoothAdapter.cancelDiscovery();
            }
            mBluetoothAdapter.startDiscovery();
        }
    }

    public void connectDevice(String address, boolean secure) {
        if(isBluetoothAvailable()) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            mChatService.connect(device, secure);
        }
    }

    public void stop(){
        mChatService.stop();
    }

    public void sendMessage(String msg){
        if(mChatService != null && mChatService.getState() == BluetoothChatService.STATE_CONNECTED){
            mChatService.write(msg.getBytes());
        } else {
            Toast.makeText(mActivity, "Bluetooth unavailable! Unable to send message.", Toast.LENGTH_SHORT);
        }
    }

    public static String getDeviceName(){
        return mChatService.getDeviceName();
    }

    public static String getDeviceAddress(){
        return mChatService.getDeviceAddress();
    }

    public static Boolean isConnected() {
        return mChatService.getState() == mChatService.STATE_CONNECTED;
    }

    public static Boolean isBluetoothAvailable(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    /**
     * Handler handling events from BluetoothChatService and passing it to MainActivity's handler
     */
    private final Handler nHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what == Constants.MESSAGE_STATE_CHANGE && msg.arg1 == BluetoothChatService.STATE_CONNECTED){
                if(mChatService.getDeviceName() != null && mChatService.getDeviceAddress() != null)
                storeDeviceRecord(mChatService.getDeviceName(), mChatService.getDeviceAddress());
            }
            Message newMsg = _mHandler.obtainMessage(msg.what);
            newMsg.copyFrom(msg);
            _mHandler.sendMessage(newMsg);
            return true;
        }
    });

    // store last connected device
    private void storeDeviceRecord(String deviceName, String deviceAddress){
        // store in SharedPreferences
        SharedPreferences settings = mActivity.getSharedPreferences(LAST_DEVICE, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(LAST_DEVICE_NAME, deviceName);
        editor.putString(LAST_DEVICE_ADDRESS, deviceAddress);
        editor.apply();
    }

    public String[] retrieveDeviceRecord(){
        // Get from the SharedPreferences
        SharedPreferences settings = mActivity.getSharedPreferences(LAST_DEVICE, 0);
        String deviceName = settings.getString(LAST_DEVICE_NAME, null);
        String deviceAddress = settings.getString(LAST_DEVICE_ADDRESS, null);
        String[] returnVal = {deviceName, deviceAddress};
        return returnVal;
    }
}
