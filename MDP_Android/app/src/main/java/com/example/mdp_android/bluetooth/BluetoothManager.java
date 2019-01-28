package com.example.mdp_android.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
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
    private Activity mActivity;
    public static final int BT_REQUEST_CODE = 0; // code for detecting whether bluetooth is switched on

    public BluetoothManager(Activity activity, Handler mHandler){
        mActivity = activity;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mChatService = new BluetoothChatService(activity, mHandler);
        _instance = this;
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
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                mActivity.startActivityForResult(enableBtIntent, BluetoothManager.BT_REQUEST_CODE);
                return false;
            }
            return true;
        }
    }

    public static BluetoothManager getInstance(){
        return _instance;
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
            // Attempt to connect to the device
            mChatService.connect(device, secure);
        }
    }

    public void disconnectDevice(){
        if(mChatService != null && mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
            mChatService.stop();
        }
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

    public void stop(){
        mChatService.stop();
    }

    public static Boolean isBluetoothAvailable(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }
}
