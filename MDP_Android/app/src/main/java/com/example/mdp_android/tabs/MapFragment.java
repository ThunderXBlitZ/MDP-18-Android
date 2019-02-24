package com.example.mdp_android.tabs;

import android.os.Bundle;
import android.os.Handler;
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

import java.util.Timer;
import java.util.TimerTask;

public class MapFragment extends Fragment implements MainActivity.CallbackFragment {
    private static final String TAG = "MapFragment";
    private Maze maze;
    private Boolean _autoRefresh = false;

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
                } else if (maze.getState() == Constants.coordinateMode && maze.coordinatesSet()) {
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
                    BluetoothManager.getInstance().sendMessage("beginExplore", "");
                    maze.setState(Constants.exploreMode);
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
                    BluetoothManager.getInstance().sendMessage("waypoints", maze.getWaypointList());
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
                    BluetoothManager.getInstance().sendMessage("beginManual", "");
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
                if (maze.getState() == Constants.idleMode && maze.isExploreCompleted()) {
                    // start recording time
                    Toast.makeText(getActivity(), "Sending robot on fastest path!", Toast.LENGTH_SHORT).show();
                    getView().findViewById(R.id.waypoint_button).setEnabled(false);
                    BluetoothManager.getInstance().sendMessage("beginFastest", "");
                    maze.setState(Constants.fastestPathMode);
                }
            }
        });

        // reset button
        getView().findViewById(R.id.resetBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (maze.getState() == Constants.idleMode) {
                    Toast.makeText(getActivity(), "Maze reset!", Toast.LENGTH_SHORT).show();
                    BluetoothManager.getInstance().sendMessage("reset", "");
                    maze.reset();
                    initializeButtons();
                }
            }
        });

        // directional buttons
        getView().findViewById(R.id.leftBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (maze.getState() == Constants.manualMode) {
                    maze.attemptMoveBot(Constants.WEST);
                }
            }
        });

        getView().findViewById(R.id.rightBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (maze.getState() == Constants.manualMode) {
                    maze.attemptMoveBot(Constants.EAST);
                }
            }
        });

        getView().findViewById(R.id.upBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (maze.getState() == Constants.manualMode) {
                    maze.attemptMoveBot(Constants.NORTH);
                }
            }
        });

        getView().findViewById(R.id.downBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (maze.getState() == Constants.manualMode) {
                    maze.attemptMoveBot(Constants.SOUTH);
                }
            }
        });

        // status button
        getView().findViewById(R.id.statusBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothManager.getInstance().sendMessage("getStatus", "");
            }
        });

        // manual refresh button
        getView().findViewById(R.id.updateBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothManager.getInstance().sendMessage("sendArena", "");
            }
        });

        // auto button
        getView().findViewById(R.id.autoBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // on/off thread to auto call for maze updates
                _autoRefresh = !_autoRefresh;
                if (_autoRefresh) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable(){
                        public void run(){
                            BluetoothManager.getInstance().sendMessage("sendArena","");
                            if(_autoRefresh) handler.postDelayed(this, 3000);
                        }
                    }, 3000);
                }
            }
        });
    }

    /* Handle Bluetooth messages received */
    public void update(int type, String key, String msg) {
        switch (type) {
            case Constants.MESSAGE_STATE_CHANGE: // bluetooth state change, not required actually
                // Log.d("MESSAGE_STATE_CHANGE", msg);
                break;
            case Constants.MESSAGE_READ: // received message
                if(key == "exploreData"){
                    maze.handleExplore(msg);
                }
                else if(key == "exploreDone"){
                    // update text
                    getView().findViewById(R.id.waypoint_button).setEnabled(true);
                    getView().findViewById(R.id.fastestBtn).setEnabled(true);
                    maze.setState(Constants.idleMode);
                    Toast.makeText(getActivity(), "Exploration completed!", Toast.LENGTH_SHORT).show();
                } else if(key == "fastestPathData"){
                    maze.handleFastestPath(msg);
                } else if(key == "grid"){
                    maze.handleAMDGrid(msg);
                }
                else if(key == "fastestPathMoveDone"){
                    // update time display
                    // click to reset
                    maze.setState(Constants.idleMode);
                    getView().findViewById(R.id.exploreBtn).setEnabled(false);
                    getView().findViewById(R.id.manualBtn).setEnabled(false);
                    getView().findViewById(R.id.fastestBtn).setEnabled(false);
                    Toast.makeText(getActivity(), "Fastest Path completed!", Toast.LENGTH_SHORT).show();
                } else if (key == "robotPos"){
                    String tmp[] = msg.substring(1, msg.length()-1).split(",");
                    maze.updateBotPos(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]));
                } else if (key == "robotDir"){
                    maze.updateBotDir(msg);
                } else if (key == "status"){
                    TextView tv = getView().findViewById(R.id.statusText);
                    tv.setText(msg);
                } else if (key == "arrowBlockUp") {
                    maze.handleArrowBlock(Constants.NORTH, msg);
                } else if (key == "arrowBlockDown") {
                    maze.handleArrowBlock(Constants.SOUTH, msg);
                } else if (key == "arrowBlockLeft") {
                    maze.handleArrowBlock(Constants.WEST, msg);
                } else if (key == "arrowBlockRight") {
                    maze.handleArrowBlock(Constants.EAST, msg);
                }
                break;
        }
    }
}
