package com.example.mdp_android.tabs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.mdp_android.MainActivity;
import com.example.mdp_android.Maze;
import com.example.mdp_android.R;
import com.example.mdp_android.bluetooth.BluetoothManager;
import com.example.mdp_android.bluetooth.Constants;

public class MapFragment extends Fragment implements MainActivity.CallbackFragment {
    private static final String TAG = "MapFragment";

    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.activity_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // maze
        setupMaze(15, 20);
    }

    /**
     * Creates 'maze' container viewgroup and initalizes it with x * y number of tiles
     *
     * @param numTileWidth  number of tiles in breadth of maze
     * @param numTileHeight number of tiles in height of maze
     */
    private void setupMaze(int numTileWidth, int numTileHeight) {
        RelativeLayout mazeLayout = getView().findViewById(R.id.mazeLayout);
        Maze maze = new Maze(getActivity(), numTileWidth, numTileHeight);
        mazeLayout.addView(maze);
    }

    // Bluetooth functions
    // TBD: make this a non-layout function, so we can accept Strings as params
    public void sendMessage(View v) {
        String msg = "Hi I am Android";
        BluetoothManager.getInstance().sendMessage(msg);
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
