package com.example.mdp_android.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mdp_android.MainActivity;
import com.example.mdp_android.R;
import com.example.mdp_android.Util;

import java.util.ArrayList;

public class BluetoothFragment extends DialogFragment {

    private EditText mEditText;
    private static BluetoothChatService mChatService;
    private static BluetoothManager mMgr;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothArrayAdapter<DeviceDetails> mAdapter;
    private static BluetoothFragment mFrag;

    public BluetoothFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static BluetoothFragment newInstance(String title) {
        mFrag = new BluetoothFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        mFrag.setArguments(args);
        mMgr = BluetoothManager.getInstance();
        return mFrag;
    }

    // singleton
    public static BluetoothFragment getInstance(){
        return mFrag;
    }

    public void updateUI(int type, String value){
        if(type == Constants.MESSAGE_DEVICE_ADDRESS){
            // Show connected status on item in List View based on address;
        } else if (type == Constants.MESSAGE_STATE_CHANGE){
            TextView textView = getView().findViewById(R.id.bt_status_text_frag);
            textView.setText(value);
        }
    }

    // bluetooth functions
    public void connectDevice(String address, Boolean secure){
        mMgr.connectDevice(address, secure);
        ListView lv = (ListView)getView().findViewById(R.id.listView);
        lv.setAdapter(mAdapter);
    }

    public void disconnectDevice(){
        mMgr.disconnectDevice();
    }

    public void listBluetoothDevices(){
        mMgr.listBluetoothDevices();
    }

    // creation methods
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bluetooth_fragment, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // bluetooth config
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(nReceiver, filter);

        mAdapter = new BluetoothArrayAdapter<DeviceDetails>(getActivity(), new ArrayList<DeviceDetails>());
        ListView lv = (ListView)view.findViewById(R.id.listView);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mMgr.setupBluetooth() && mBluetoothAdapter.isDiscovering()){
                    mBluetoothAdapter.cancelDiscovery();
                }
                DeviceDetails mDevice = (DeviceDetails) mAdapter.getItem(position);
                if(mDevice.getConnected()){
                    disconnectDevice();
                } else {
                    connectDevice(mDevice.getAddress(), true);
                }
            }
        });
        lv.setAdapter(mAdapter);

        // hack
        if(BluetoothManager.isConnected()){
            DeviceDetails mConnected = new DeviceDetails(BluetoothManager.getDeviceName(), BluetoothManager.getDeviceAddress(), true);
            mAdapter.add(mConnected);
            TextView textView = getView().findViewById(R.id.bt_status_text_frag);
            textView.setText("Connected to: "+BluetoothManager.getDeviceName());
        }

        // dialog fragment config
        String title = getArguments().getString("title", "Bluetooth Menu");
        getDialog().setTitle(title);

        /*
        // Show soft keyboard automatically and request focus to field
        mEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        */
    }

    @Override
    public void onResume() {
        super.onResume();
        /* sets size of DialogFragment
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = Util.getScreenWidth() - 80;
        params.height = Util.getScreenHeight() - 80;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        */
        setupToolbar();
        refreshList();
    }

    public void refreshList(){
        ListView lv = (ListView)getView().findViewById(R.id.listView);
        lv.setAdapter(mAdapter);
    }

    private void setupToolbar(){
        Toolbar toolbar = getView().findViewById(R.id.toolbar);
        toolbar.setTitle("Bluetooth Menu");

        // include a back icon
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        // handle menu item click
        toolbar.inflateMenu(R.menu.fragment_toolbar_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()){
                    case R.id.scan_btn:
                        listBluetoothDevices();
                }
                return true;
            }
        });
    }
    /**
     * Receiver for broadcast events from the system, mostly bluetooth related
     * Creates list of Bluetooth Devices detected
     */
    private final BroadcastReceiver nReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Toast.makeText(getActivity(), "Discovery started", Toast.LENGTH_SHORT).show();
                mAdapter.clear();
            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                DeviceDetails newDevice = new DeviceDetails(device.getName(), device.getAddress(), false);
                mAdapter.add(newDevice);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMgr.setupBluetooth() && mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }
        getActivity().unregisterReceiver(nReceiver);
        mFrag = null;
    }
}