package com.example.mdp_android.tabs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mdp_android.MainActivity;
import com.example.mdp_android.R;

public class CommFragment extends Fragment implements MainActivity.CallbackFragment {
    private static final String TAG ="CommFragment";

    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.activity_communication, container, false);
    }

    public void update(String type, String msg){
        switch("type"){
            case "Bluetooth":
                break;
            case "1": // Constants.MESSAGE_STATE_CHANGE
                break;
        }
    }
}
