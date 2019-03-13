package com.example.mdp_android.tabs;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mdp_android.Constants;
import com.example.mdp_android.MainActivity;
import com.example.mdp_android.R;
import com.example.mdp_android.bluetooth.BluetoothArrayAdapter;
import com.example.mdp_android.bluetooth.BluetoothChatService;
import com.example.mdp_android.bluetooth.BluetoothManager;
import com.example.mdp_android.bluetooth.DeviceDetails;

import java.util.ArrayList;

public class BluetoothFragment extends Fragment implements MainActivity.CallbackFragment {
    private static final String TAG = "BluetoothFragment";

    private final BluetoothManager mMgr = BluetoothManager.getInstance();
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothArrayAdapter<DeviceDetails> mAdapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_bluetooth, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){

        // bluetooth config
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(nReceiver, filter);

        Button button = view.findViewById(R.id.btn_discover);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listBluetoothDevices();
            }
        });

        mAdapter = new BluetoothArrayAdapter<DeviceDetails>(getActivity(), new ArrayList<DeviceDetails>());
        ListView lv = getView().findViewById(R.id.listView);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cancelDiscovery();
                DeviceDetails mDevice = (DeviceDetails) mAdapter.getItem(position);
                if(mDevice.getConnected()){
                    disconnectDevice();
                } else {
                    connectDevice(mDevice.getAddress(), true);
                }
            }
        });
        lv.setAdapter(mAdapter);
    }

    public void update(int type, String key, String msg){
        switch(type) {
            case Constants.MESSAGE_STATE_CHANGE:
                if(getView() != null){
                    TextView textView = getView().findViewById(R.id.bt_status_text_frag);
                    if(textView != null) textView.setText(msg);
                    break;
                }
        }

        if(!BluetoothManager.getInstance().bluetoothAvailable() && mAdapter != null){
            mAdapter.clear();
        }
        if(mAdapter != null && mAdapter.getCount() == 0){
            displayConnectedDevice();
        }
        refreshList();
    }

    // bluetooth functions
    public void connectDevice(String address, Boolean secure){
        mMgr.connectDevice(address, secure);
        refreshList();
    }

    public void disconnectDevice(){
        mMgr.stop();
    }

    public void listBluetoothDevices(){
        mMgr.listBluetoothDevices();
    }


    /**
     * Shows the current connected device as first item in the list.
     */
    private void displayConnectedDevice(){
        if(BluetoothManager.getInstance().isConnected()){
            DeviceDetails mConnected = new DeviceDetails(BluetoothManager.getInstance().getDeviceName(), BluetoothManager.getInstance().getDeviceAddress(), true);
            mAdapter.clear();
            mAdapter.add(mConnected);
            TextView textView = getView().findViewById(R.id.bt_status_text_frag);
            textView.setText("Connected to: "+BluetoothManager.getInstance().getDeviceName());
        }
    }

    private void refreshList(){
        if (getView() != null) {
            ListView lv = getView().findViewById(R.id.listView);
            if(lv != null){
                lv.setAdapter(null);
                lv.setAdapter(mAdapter);
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        refreshList();
        displayConnectedDevice();
    }


    /**
     * Receiver for broadcast events from the system, mostly bluetooth related
     * Creates list of Bluetooth Devices detected
     */
    private ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
    private final BroadcastReceiver nReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Toast.makeText(getActivity(), "Discovery started", Toast.LENGTH_SHORT).show();
                mAdapter.clear();
                deviceList.clear();
            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                DeviceDetails newDevice = new DeviceDetails(device.getName(), device.getAddress(), false);
                if(device.getName() == null /* || (!device.getName().contains("rasp")  && !device.getName().contains("DESKTOP"))*/ ) return;
                Log.i("discovery", newDevice.toString());
                mAdapter.add(newDevice);
                deviceList.add(device);
            }
        }
    };

    // clean up methods
    private void cancelDiscovery(){
        if (mMgr.bluetoothAvailable() && mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelDiscovery();
        try {
            getActivity().unregisterReceiver(nReceiver);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
    }
}
