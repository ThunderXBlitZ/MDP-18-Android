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
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.widget.Toolbar;


import com.example.mdp_android.bluetooth.BluetoothManager;
import com.example.mdp_android.tabs.SectionPageAdapter;

public class MainActivity extends AppCompatActivity {
    private BluetoothManager mBluetoothMgr;
    private SectionPageAdapter mSectionPageAdapter;
    private PagerAdapter pagerAdapter;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        setupMaze(10, 10);
//
//
//        // Only ask for these permissions on runtime when running Android 6.0 or higher
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






//        //Bluetooth
//        mBluetoothMgr = new BluetoothManager(this, mHandler);
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
//        registerReceiver(mReceiver, filter);
//
//        // request Bluetooth to be switched on
//        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//        startActivityForResult(enableBtIntent, BluetoothManager.BT_REQUEST_CODE);
//    }
//
//    /**
//     * Creates 'maze' container viewgroup and initalizes it with x * y number of tiles
//     *
//     * @param numTileWidth  number of tiles in breadth of maze
//     * @param numTileHeight number of tiles in height of maze
//     */
//    private void setupMaze(int numTileWidth, int numTileHeight) {
//        RelativeLayout mazeLayout = (RelativeLayout) findViewById(R.id.mazeLayout);
//        Maze maze = new Maze(this, numTileWidth, numTileHeight);
//        mazeLayout.addView(maze);
//    }
//
//    // Bluetooth functions
//    // TBD: make this a non-layout function, so we can accept Strings as params
//    public void sendMessage(View v) {
//        String msg = "Hi I am Android";
//        mBluetoothMgr.sendMessage(msg);
//    }
//
//    /**
//     * Receiver for broadcast events from the system, mostly bluetooth related
//     */
//    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
//                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
//                        BluetoothAdapter.ERROR);
//                BluetoothFragment mFrag = BluetoothFragment.getInstance();
//                switch (state) {
//                    case BluetoothAdapter.STATE_OFF:
//                        mBluetoothMgr.stop();
//                        if (mFrag != null) {
//                            mFrag.updateUI(-1, null);
//                        }
//                        break;
//                    case BluetoothAdapter.STATE_TURNING_OFF:
//                        // do nothing
//                        break;
//                    case BluetoothAdapter.STATE_ON:
//                        String[] deviceRecord = mBluetoothMgr.retrieveDeviceRecord();
//                        if (deviceRecord[0] != null && deviceRecord[1] != null) {
//                            mBluetoothMgr.connectDevice(deviceRecord[1], true);
//                            Toast.makeText(getApplicationContext(), "Bluetooth turned on! Auto-connecting...", Toast.LENGTH_SHORT).show();
//                        }
//                        break;
//                    case BluetoothAdapter.STATE_TURNING_ON:
//                        // do nothing
//                        break;
//                }
//            }
//        }
//    };
//
//    /**
//     * The Handler that gets information back from the BluetoothChatService
//     * and updates MainActivity/BluetoothFragment
//     */
//    private final Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            BluetoothFragment mFrag = BluetoothFragment.getInstance();
//            switch (msg.what) {
//                case Constants.MESSAGE_STATE_CHANGE:
//                    TextView textView = findViewById(R.id.bt_status_text
//                    );
//                    switch (msg.arg1) {
//                        case BluetoothChatService.STATE_CONNECTED:
//                            String tmp = "Connected to: " + mBluetoothMgr.getDeviceName();
//                            textView.setText(tmp);
//                            Toast.makeText(MainActivity.this, tmp, Toast.LENGTH_SHORT).show();
//                            if (mFrag != null) {
//                                mFrag.updateUI(Constants.MESSAGE_STATE_CHANGE, tmp);
//                            }
//                            break;
//                        case BluetoothChatService.STATE_CONNECTING:
//                            textView.setText("Connecting...");
//                            if (mFrag != null) {
//                                mFrag.updateUI(Constants.MESSAGE_STATE_CHANGE, "Connecting...");
//                            }
//                            break;
//                        case BluetoothChatService.STATE_LOST:
//                            if (mFrag != null) {
//                                mFrag.updateUI(Constants.MESSAGE_STATE_CHANGE, "Disconnected");
//                            }
//                            Toast.makeText(MainActivity.this, "Connection Lost!", Toast.LENGTH_SHORT).show();
//                            break;
//                        //case BluetoothChatService.STATE_LISTEN:
//                        case BluetoothChatService.STATE_NONE:
//                            if (mFrag != null) {
//                                mFrag.updateUI(Constants.MESSAGE_STATE_CHANGE, "Not Connected");
//                            }
//                            textView.setText("Not Connected");
//                            break;
//
//                    }
//                    break;
//                case Constants.MESSAGE_WRITE:
//                    // dont do anything
//                    /*
//                    byte[] writeBuf = (byte[]) msg.obj;
//                    // construct a string from the buffer
//                    String writeMessage = new String(writeBuf);
//                    */
//                    break;
//                case Constants.MESSAGE_READ:
//                    byte[] readBuf = (byte[]) msg.obj;
//                    // construct a string from the valid bytes in the buffer
//                    String readMessage = new String(readBuf, 0, msg.arg1);
//                    Toast.makeText(MainActivity.this, "read: " + readMessage, Toast.LENGTH_SHORT).show();
//                    break;
//                /*
//                case Constants.MESSAGE_DEVICE_NAME:
//                    // save the connected device's name
//                    String mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
//                    String mConnectedAddr = msg.getData().getString(Constants.DEVICE_ADDRESS);
//                    // display in fragment if any
//                    if(mFrag != null){
//                        mFrag.updateUI(Constants.MESSAGE_DEVICE_ADDRESS, mConnectedAddr);
//                    }
//                    break;
//                */
//                case Constants.MESSAGE_TOAST:
//                    Toast.makeText(MainActivity.this, msg.getData().getString(Constants.TOAST),
//                            Toast.LENGTH_SHORT).show();
//                    break;
//            }
//        }
//    };
//
//    // creation methods
//    private void showBluetoothFragment() {
//        FragmentManager fm = getSupportFragmentManager();
//        BluetoothFragment bluetoothDialogFragment = BluetoothFragment.newInstance("Bluetooth Menu");
//        bluetoothDialogFragment.show(fm, "bluetooth_fragment");
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.bluetooth_menu) {
//            if (mBluetoothMgr.setupBluetooth())
//                showBluetoothFragment();
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    // cleanup methods
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        unregisterReceiver(mReceiver);
//        mBluetoothMgr.stop();
//    }
//    //end of class


    }

}
