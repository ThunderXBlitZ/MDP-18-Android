package com.example.mdp_android;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<DeviceDetails> deviceItemList;
    private ArrayAdapter<DeviceDetails> mAdapter;
    private BluetoothChatService mChatService;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
                mAdapter.clear();
            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d("DEVICELIST", "Bluetooth device found\n");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Create a new device item
                DeviceDetails newDevice = new DeviceDetails(device.getName(), device.getAddress(), "false");
                // Add it to our adapter
                mAdapter.add(newDevice);
                mAdapter.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(context, "Scan finished!", Toast.LENGTH_SHORT).show();
                unregisterReceiver(mReceiver);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupMaze(10,10);
        setupBluetooth();

        mAdapter = new ArrayAdapter<DeviceDetails>(this, android.R.layout.simple_list_item_1);
        //mAdapter = new ArrayAdapter<BluetoothDeviceWrapper>(getApplicationContext(), android.R.layout.simple_list_item_1);;
        ListView lv = (ListView)findViewById(R.id.listView);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (_mBluetoothAdapter.isDiscovering()){
                    _mBluetoothAdapter.cancelDiscovery();
                    unregisterReceiver(mReceiver);
                }
                connectDevice(mAdapter.getItem(position).getAddress(), true);
            }
        });
        lv.setAdapter(mAdapter);
    }

    /**
     * Establish connection with other device
     *
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(String address, boolean secure) {
        BluetoothDevice device = _mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }
    /**
     * Creates 'maze' container viewgroup and initalizes it with x * y number of tiles
     * @param numTileWidth number of tiles in breadth of maze
     * @param numTileHeight number of tiles in height of maze
     */
    private void setupMaze(int numTileWidth, int numTileHeight){
        RelativeLayout mazeLayout = (RelativeLayout) findViewById(R.id.mazeLayout);
        Maze maze = new Maze(this, numTileWidth, numTileHeight);
        mazeLayout.addView(maze);
    }

    public final int BT_REQUEST_CODE = 0;
    private BluetoothAdapter _mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private void BluetoothToastPrompt(){
        Toast.makeText(getApplicationContext(), "Please enable Bluetooth and try again!",Toast.LENGTH_SHORT).show();
    }

    private Boolean setupBluetooth(){
        if (_mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth not supported on this device!",Toast.LENGTH_SHORT).show();
            return false;
        }
        else{
            if(!_mBluetoothAdapter.isEnabled()){
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, BT_REQUEST_CODE);
                return false;
                }
                else {
                // start BluetoothChatService since Bluetooth is enabled
                if(mChatService == null){
                    mChatService = new BluetoothChatService(MainActivity.this, readHandler);
                    Toast.makeText(this, "started BT service", Toast.LENGTH_SHORT);
                } else {
                    if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                        mChatService.start();
                        Toast.makeText(this, "started BT service", Toast.LENGTH_SHORT);
                    }
                }
                return true;
            }
        }
    }

    /**
     * Android function for handling results of any subprocess/activities/intents
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == BT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth turned on!",Toast.LENGTH_SHORT).show();
            } else {
                BluetoothToastPrompt();
                }
        }
    }

    public void listBluetoothDevices(View v){
        if(setupBluetooth()){

            Toast.makeText(getApplicationContext(), "Button clicked & BT on",Toast.LENGTH_SHORT).show();

            // Register for broadcasts when a device is discovered.
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(mReceiver, filter);

            if (_mBluetoothAdapter.isDiscovering()){
                _mBluetoothAdapter.cancelDiscovery();
                unregisterReceiver(mReceiver);
            }
            _mBluetoothAdapter.startDiscovery();
        }
    }

    /* Bonded
    public void listBluetoothDevices(View v){
        if(setupBluetooth()){
            ListView lv = (ListView)findViewById(R.id.listView);
            Set<BluetoothDevice>pairedDevices;
            pairedDevices = _mBluetoothAdapter.getBondedDevices();

            ArrayList list = new ArrayList();
            if(pairedDevices != null){
                for(BluetoothDevice bt : pairedDevices){
                    list.add(bt.getName());
                }
                Toast.makeText(getApplicationContext(), "Showing Paired Devices",Toast.LENGTH_SHORT).show();
                final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
                lv.setAdapter(adapter);
            }
        }
    }
    */

    public void setStatus(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler readHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            Toast.makeText(MainActivity.this, "connected to device", Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            Toast.makeText(MainActivity.this, "connecting to device", Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            Toast.makeText(MainActivity.this, "not connected", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Toast.makeText(MainActivity.this, "write: "+writeMessage, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Toast.makeText(MainActivity.this, "read: "+readMessage, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    String mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                        Toast.makeText(MainActivity.this, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_TOAST:
                        Toast.makeText(MainActivity.this, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}