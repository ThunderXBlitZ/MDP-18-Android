package com.example.mdp_android;

import com.example.mdp_android.tabs.BluetoothFragment;
import com.example.mdp_android.tabs.CommFragment;
import com.example.mdp_android.tabs.MapFragment;

public class MockRPI {
    private static MockRPI _instance;
    private MapFragment _mapFrag;
    private BluetoothFragment _btFrag;
    private CommFragment _commFrag;

    public void mockRPI(MapFragment mapFrag, BluetoothFragment btFrag, CommFragment commFrag){
        _mapFrag = mapFrag;
        _btFrag = btFrag;
        _commFrag = commFrag;
    }

    public static MockRPI getInstance(){
        return _instance;
    }

    public void receivedEvent(String msg){
        // convert to JSON
        // switch type, send json message back to relevant fragment
    }
}
