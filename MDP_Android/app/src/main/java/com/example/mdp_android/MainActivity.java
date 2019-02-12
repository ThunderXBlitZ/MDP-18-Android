package com.example.mdp_android;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.widget.Toast;
import android.widget.Toolbar;


import com.example.mdp_android.bluetooth.BluetoothChatService;
import com.example.mdp_android.bluetooth.BluetoothManager;
import com.example.mdp_android.tabs.SectionPageAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private BluetoothManager mBluetoothMgr;

    private SectionPageAdapter mSectionPageAdapter;
    private PagerAdapter pagerAdapter;

    private ArrayList<CallbackFragment> callbackFragList = new ArrayList<CallbackFragment>();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Only ask for these permissions on runtime when running Android 6.0 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }

        //Tabs
        Toolbar toolbar = findViewById(R.id.toolbar);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        TabItem tabMap = findViewById(R.id.map);
        TabItem tabComm = findViewById(R.id.comm);
        TabItem tabBluetooth = findViewById(R.id.bluetooth);
        final ViewPager mViewPager = findViewById(R.id.container);
        SectionPageAdapter pageAdapter = new SectionPageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());

        mViewPager.setAdapter(pageAdapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        // Bluetooth
        mBluetoothMgr = new BluetoothManager(this, mHandler);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        // request Bluetooth to be switched on
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, BluetoothManager.BT_REQUEST_CODE);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if(fragment instanceof CallbackFragment){
            callbackFragList.add((CallbackFragment) fragment);
        }
    }

    /**
     * Receiver for broadcast events from the system, mostly bluetooth related
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        mBluetoothMgr.stop();
                        // notifyFragments("Bluetooth", "off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        // do nothing
                        break;
                    case BluetoothAdapter.STATE_ON:
                        String[] deviceRecord = mBluetoothMgr.retrieveDeviceRecord();
                        if (deviceRecord[0] != null && deviceRecord[1] != null) {
                            mBluetoothMgr.connectDevice(deviceRecord[1], true);
                            Toast.makeText(getApplicationContext(), "Bluetooth turned on! Auto-connecting...", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        // do nothing
                        break;
                }
            }
        }
    };

    /**
     * The Handler that gets information back from the BluetoothChatService
     * and updates MainActivity/BluetoothFragment
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            String tmp = "Connected to: " + mBluetoothMgr.getDeviceName();
                            Toast.makeText(MainActivity.this, tmp, Toast.LENGTH_SHORT).show();
                            notifyFragments(String.valueOf(Constants.MESSAGE_STATE_CHANGE), tmp);
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            notifyFragments(String.valueOf(Constants.MESSAGE_STATE_CHANGE), "Connecting...");
                            break;
                        case BluetoothChatService.STATE_LOST:
                            notifyFragments(String.valueOf(Constants.MESSAGE_STATE_CHANGE), "Connection Lost!");
                            Toast.makeText(MainActivity.this, "Connection Lost!", Toast.LENGTH_SHORT).show();
                            break;
                        //case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            notifyFragments(String.valueOf(Constants.MESSAGE_STATE_CHANGE), "Not Connected");
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    // dont do anything
                    /*
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    */
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Toast.makeText(MainActivity.this, "read: " + readMessage, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(MainActivity.this, msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    // cleanup methods
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        mBluetoothMgr.stop();
    }
    //end of class

    // for handling callbacks from BluetoothChatService to the Tab Fragments
    public interface CallbackFragment {
        public void update(String type, String msg);
    }

    public void notifyFragments(String type, String msg){
        for(CallbackFragment i:callbackFragList){
            i.update(type, msg);
        }
    }
}
