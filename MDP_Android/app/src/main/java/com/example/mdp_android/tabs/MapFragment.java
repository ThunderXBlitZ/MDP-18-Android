package com.example.mdp_android.tabs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mdp_android.Constants;
import com.example.mdp_android.MainActivity;
import com.example.mdp_android.Maze;
import com.example.mdp_android.R;
import com.example.mdp_android.bluetooth.BluetoothManager;

import org.json.JSONArray;
import org.json.JSONObject;

public class MapFragment extends Fragment implements MainActivity.CallbackFragment {
    private static final String TAG = "MapFragment";
    private Maze maze;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        // setHasOptionsMenu(true);
        return inflater.inflate(R.layout.activity_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // maze
        setupMaze();
        maze.setState(Constants.idleMode);

        // controls
        initializeButtons();
        setupButtonListeners();
    }

    // Bluetooth functions
    public void sendMessage(String type, String msg) {
        BluetoothManager.getInstance().sendMessage(type, msg);
    }

    public void update(int type, String key, String msg) {
        switch (type) {
            case Constants.MESSAGE_STATE_CHANGE: // bluetooth state change, not required actually
                Log.d("MESSAGE_STATE_CHANGE", msg);
                break;
            case Constants.MESSAGE_READ: // received message
                Log.d("MESSAGE_READ", msg);
                if(key.contains("moveDone") || key.contains("MoveDone")){
                    int dir = Integer.parseInt(msg);
                    if(dir == Constants.up || dir == Constants.down || dir == Constants.left || dir == Constants.right){
                        maze.moveBot(dir, true);
                    }
                }
                if(key == "exploreStepDone"){
                     if(msg == ""){ // obstacle_detected

                    } else if(msg == ""){ // arrow block detected

                    }
                    // send current state of maze to Algo
                } else if(key == "exploreDone"){
                    // update text
                    getView().findViewById(R.id.waypoint_button).setEnabled(true);
                    getView().findViewById(R.id.fastestBtn).setEnabled(true);
                    maze.setState(Constants.idleMode);
                } else if(key == "fastestPathStr"){
                    // begin sequence of actions
                } else if(key == "fastestPathMoveDone"){
                    // update time display
                    // click to reset
                    maze.setState(Constants.idleMode);
                    getView().findViewById(R.id.exploreBtn).setEnabled(false);
                    getView().findViewById(R.id.manualBtn).setEnabled(false);
                    getView().findViewById(R.id.fastestBtn).setEnabled(false);
                }
                break;
        }
    }

    /**
     * Creates 'maze' container viewgroup and initalizes it
     */
    private void setupMaze() {
        RelativeLayout mazeLayout = getView().findViewById(R.id.mazeLayout);
        maze = new Maze(getActivity());
        mazeLayout.addView(maze);
    }

    /**
     * Resets button states
     */
    private void initializeButtons() {
        getView().findViewById(R.id.coordBtn).setEnabled(true);
        getView().findViewById(R.id.waypoint_button).setEnabled(false);
        getView().findViewById(R.id.exploreBtn).setEnabled(true);
        getView().findViewById(R.id.manualBtn).setEnabled(false);
        getView().findViewById(R.id.fastestBtn).setEnabled(false);
        getView().findViewById(R.id.resetBtn).setEnabled(true);
    }

    private void setupButtonListeners() {
        // coordinates button
        getView().findViewById(R.id.coordBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (maze.getState() == Constants.idleMode) {
                    Toast.makeText(getActivity(), "Tap on tiles to set your start and end coordinates", Toast.LENGTH_SHORT).show();
                    maze.setState(Constants.coordinateMode);
                } else if (maze.getState() == Constants.coordinateMode) {
                    Toast.makeText(getActivity(), "Exiting coordinates mode...", Toast.LENGTH_SHORT).show();
                    maze.setState(Constants.idleMode);
                    getView().findViewById(R.id.manualBtn).setEnabled(true);
                }
            }
        });

        // explore button
        getView().findViewById(R.id.exploreBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (maze.getState() == Constants.idleMode && maze.coordinatesSet()) {
                    Toast.makeText(getActivity(), "Starting exploration!", Toast.LENGTH_SHORT).show();
                    getView().findViewById(R.id.coordBtn).setEnabled(false);

                    // sendMessage("", "beginExplore");
                    maze.setState(Constants.exploreMode);
                    maze.explore();
                }
            }
        });

        // waypoint button
        getView().findViewById(R.id.waypoint_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (maze.getState() == Constants.idleMode) {
                    maze.clearWaypoints();
                    maze.setState(Constants.waypointMode);
                    Toast.makeText(getActivity(), "Tap on tiles to set any waypoints", Toast.LENGTH_SHORT).show();
                }
                // finished setting waypoints
                else if (maze.getState() == Constants.waypointMode) {
                    maze.setState(Constants.idleMode);
                    Toast.makeText(getActivity(), "Exiting waypoint mode...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // manual control button
        getView().findViewById(R.id.manualBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (maze.getState() == Constants.idleMode && maze.coordinatesSet()) {
                    Toast.makeText(getActivity(), "Entering manual mode: control robot by tapping tiles or arrow buttons.", Toast.LENGTH_SHORT).show();
                    getView().findViewById(R.id.waypoint_button).setEnabled(false);
                    maze.setState(Constants.manualMode);
                } else if (maze.getState() == Constants.manualMode) {
                    Toast.makeText(getActivity(), "Exiting Manual Mode!", Toast.LENGTH_SHORT).show();
                    maze.setState(Constants.idleMode);
                }
            }
        });

        // fastest path button
        getView().findViewById(R.id.fastestBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (maze.getState() == Constants.idleMode && maze.exploreCompleted()) {

                    // start recording time?
                    //sendMessage("MESSAGE_COMMAND", "...");
                    // ... should be "begin_fastest" and maze array as string

                    Toast.makeText(getActivity(), "Sending robot on fastest path!", Toast.LENGTH_SHORT).show();
                    getView().findViewById(R.id.waypoint_button).setEnabled(false);

                    maze.setState(Constants.fastestPathMode);
                    maze.fastestPath();
                }
            }
        });

        // reset button
        getView().findViewById(R.id.resetBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (maze.getState() == Constants.idleMode) {
                    Toast.makeText(getActivity(), "Maze reset!", Toast.LENGTH_SHORT).show();
                    maze.reset();
                    initializeButtons();
                    maze.setState(Constants.idleMode);
                }
            }
        });

        // directional buttons
        getView().findViewById(R.id.leftBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (maze.getState() == Constants.manualMode) {
                    sendMessage("move", "sl");
                    // maze.moveBot(Constants.left, true); // to change to send bluetooth command
                }
            }
        });

        getView().findViewById(R.id.rightBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (maze.getState() == Constants.manualMode) {
                    sendMessage("move", "sr");
                    // maze.moveBot(Constants.right, true); // to change to send bluetooth command
                }
            }
        });

        getView().findViewById(R.id.upBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (maze.getState() == Constants.manualMode) {
                    sendMessage("move", "f");
                    // maze.moveBot(Constants.up, true); // to change to send bluetooth command
                }
            }
        });

        getView().findViewById(R.id.downBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (maze.getState() == Constants.manualMode) {
                    sendMessage("move", "r");
                    // maze.moveBot(Constants.down, true); // to change to send bluetooth command
                }
            }
        });

        // temp
        getView().findViewById(R.id.updateBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("", "sendArena");
            }
        });
    }
}
