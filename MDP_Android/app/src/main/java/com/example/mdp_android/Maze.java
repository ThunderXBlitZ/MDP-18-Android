package com.example.mdp_android;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.mdp_android.bluetooth.BluetoothManager;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Maze extends ViewGroup {
    public static int TILESIZE = 0;
    private static final int MAZE_WIDTH = 15;
    private static final int MAZE_HEIGHT = 20;
    private ArrayList<MazeTile> _tileList;
    private int _inputState = 0;
    private Boolean _coordinatesSet = false;
    private Boolean _exploreCompleted = false;
    private int[] _botCoord = {0, 0};
    private int[] _startCoord = {0, 0};
    private int[] _endCoord = {0, 0};
    private int _coordCount = 0;
    private ArrayList<Integer[]> _waypointList = new ArrayList<Integer[]>();

    /**
     * Constructor for maze. Creates 15 * 20 number of tiles and stores in arrayList '_tileList'
     *
     * @param context
     */
    public Maze(Context context) {
        super(context);
        _tileList = new ArrayList<MazeTile>(MAZE_WIDTH * MAZE_HEIGHT);

        // generate mazeTiles, save to arraylist
        int i, j;
        for (i = 0; i < MAZE_HEIGHT; i++) {
            for (j = 0; j < MAZE_WIDTH; j++) {
                MazeTile mazeTile = new MazeTile(context, j, i);
                this.addView(mazeTile);
                _tileList.add(mazeTile);
                mazeTile.setOnClickListener(_tileListener);
            }
        }
    }

    public int getState() {
        return _inputState;
    }

    public void setState(int newState) {
        _inputState = newState;
    }

    public boolean coordinatesSet() {
        return _coordinatesSet;
    }

    public boolean exploreCompleted() {
        return _exploreCompleted;
    }

    private void placeBot() {
        int[] _botHeadCoord = _botCoord.clone();
        _botHeadCoord[1] = _botCoord[1] - 1; // facing north
        setBotTiles(_botCoord, _botHeadCoord, true);
    }

    public void moveBot(int direction, Boolean isExplore) {
        int[] _botHeadCoord = _botCoord.clone();

        if (direction == Constants.left && _botCoord[0] > 1) {
            clearBot(isExplore);
            _botCoord[0] -= 1;
            _botHeadCoord[0] = _botCoord[0] - 1;
            setBotTiles(_botCoord, _botHeadCoord, false);
        } else if (direction == Constants.up && _botCoord[1] > 1) {
            clearBot(isExplore);
            _botCoord[1] -= 1;
            _botHeadCoord[1] = _botCoord[1] - 1;
            setBotTiles(_botCoord, _botHeadCoord, false);
        } else if (direction == Constants.right && _botCoord[0] < MAZE_WIDTH - 2) {
            clearBot(isExplore);
            _botCoord[0] += 1;
            _botHeadCoord[0] = _botCoord[0] + 1;
            setBotTiles(_botCoord, _botHeadCoord, false);
        } else if (direction == Constants.down && _botCoord[1] < MAZE_HEIGHT - 2) {
            clearBot(isExplore);
            _botCoord[1] += 1;
            _botHeadCoord[1] = _botCoord[1] + 1;
            setBotTiles(_botCoord, _botHeadCoord, false);
        }
    }

    // removes bot from maze by resetting tiles' states
    private void clearBot(Boolean isExplore) {
        // if doing exploration, replace bot by 'explored' tiles as part of exploration
        if (isExplore) {
            int[] correctedCoord = correctSelectedTile(_botCoord[0], _botCoord[1], 0);
            ArrayList<MazeTile> targetTiles = getTargetTiles(correctedCoord[0], correctedCoord[1], 0);
            setTile(targetTiles, Constants.EXPLORED, false);
        } else {
            int[] correctedCoord = correctSelectedTile(_botCoord[0], _botCoord[1], 0);
            ArrayList<MazeTile> targetTiles = getTargetTiles(correctedCoord[0], correctedCoord[1], 0);
            setTile(targetTiles, Constants.PREV, false);
        }
    }

    /**
     * do exploration
     */
    public void explore() {
        // persist state of tiles, for restoring after exploration is done
        for (MazeTile a : _tileList) {
            if (a.getState() != Constants.ROBOT_BODY && a.getState() != Constants.ROBOT_HEAD && a.getState() != Constants.EXPLORED) {
                a.forceUpdatePrevState();
            }
        }
        // do exploration stuff here ...

        // completed exploration
        _exploreCompleted = true;
        for (MazeTile a : _tileList) {
            if (a.getState() == Constants.EXPLORED) a.restorePrevState();
        }
    }

    public void fastestPath() {
        // do fastestPath
    }

    public void clearWaypoints() {
        for (Integer[] a : _waypointList) {
            ArrayList<MazeTile> targetTiles = getTargetTiles(a[0], a[1], 0);
            for (MazeTile b : targetTiles) {
                if (b.getState() == Constants.WAYPOINT) {
                    b.reset();
                } else {
                    int tempState = b.getState();
                    b.reset();
                    setTile(b, tempState, true);
                }
            }
            }
        _waypointList = new ArrayList<Integer[]>();
    }

    public void reset() {
        _inputState = Constants.idleMode;
        _coordCount = 0;
        _coordinatesSet = false;
        _exploreCompleted = false;
        clearWaypoints();
        for (MazeTile i : _tileList) {
            i.reset();
        }
    }

    private ArrayList<Integer> decimalStringToBinary(String decimalStr){
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (int i=0; i < decimalStr.length(); i++){
            Integer tmp = Character.getNumericValue(decimalStr.charAt(i));
            String tmp2 = Integer.toString(tmp,2); // decimal to binary
            for (int j=0; j < tmp2.length(); j++) {
                result.add(Character.getNumericValue(tmp2.charAt(j)));
            }
        }
        return result;
    }

    private View.OnClickListener _tileListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (v instanceof MazeTile) {
                MazeTile mazeTile = (MazeTile) v;
                if (_inputState == Constants.coordinateMode) {
                    handleCoordinatesInput(mazeTile);
                } else if (_inputState == Constants.waypointMode) {
                    handleWaypointInput(mazeTile);
                } else if (_inputState == Constants.manualMode) {
                    handleManualInput(mazeTile);
                }
            }
        }
    };

    private void handleCoordinatesInput(MazeTile mazeTile) {
        if (_coordCount == 0) {
            _coordinatesSet = false;
            // clear previously set start/end tiles
            int [] prevStartCoord = correctSelectedTile(_startCoord[0], _startCoord[1], 0);
            int [] prevEndCoord = correctSelectedTile(_endCoord[0], _endCoord[1], 0);
            ArrayList<MazeTile> targetedTiles = getTargetTiles(prevStartCoord[0], prevStartCoord[1], 0);
            targetedTiles.addAll(getTargetTiles(prevEndCoord[0], prevEndCoord[1], 0));
            setTile(targetedTiles, Constants.UNEXPLORED, false);

            // set start tiles
            _startCoord = correctSelectedTile(mazeTile.get_xPos(), mazeTile.get_yPos(), 0);
            _botCoord = _startCoord.clone();
            ArrayList<MazeTile> targetTiles = getTargetTiles(_startCoord[0], _startCoord[1], 0);
            setTile(targetTiles, Constants.START, true);
            placeBot(); // set bot at start coordinates
            _coordCount = 1;
        } else if (_coordCount == 1) {
            _endCoord = correctSelectedTile(mazeTile.get_xPos(), mazeTile.get_yPos(), 0);
            ArrayList<MazeTile> targetTiles = getTargetTiles(_endCoord[0], _endCoord[1], 0);
            setTile(targetTiles, Constants.GOAL, true);
            _coordCount = 0;
            _coordinatesSet = true;
            BluetoothManager.getInstance().sendMessage("coordinates", '('+_startCoord[0]+","+_startCoord[1]+')');
        }
    }

    private void handleWaypointInput(MazeTile mazeTile) {
        boolean safeToSet = true;
        int [] waypointCoord = correctSelectedTile(mazeTile.get_xPos(), mazeTile.get_yPos(), 0);
        ArrayList<MazeTile> targetMazeTiles = getTargetTiles(waypointCoord[0], waypointCoord[1], 0);
        // check that target tiles are not occupied by something else
        for (MazeTile a : targetMazeTiles) {
            if (a.getState() != Constants.UNEXPLORED) {
                safeToSet = false;
                break;
            }
        }
        if (safeToSet) {
            int[] waypoint = correctSelectedTile(mazeTile.get_xPos(), mazeTile.get_yPos(), 0);
            ArrayList<MazeTile> targetedTiles = getTargetTiles(waypoint[0], waypoint[1], 0);
            setTile(targetedTiles, Constants.WAYPOINT, true);
            Integer[] waypoint2 = {(Integer) waypoint[0], (Integer) waypoint[1]};
            _waypointList.add(waypoint2);
        }
    }

    private void handleManualInput(MazeTile mazeTile) {
        if (mazeTile.get_xPos() == _botCoord[0]) {
            if (mazeTile.get_yPos() == _botCoord[1] + 2) {
                moveBot(Constants.down, true); // true for testing, rmb to change to false
            } else if (mazeTile.get_yPos() == _botCoord[1] - 2) {
                moveBot(Constants.up, true);
            }
        } else if (mazeTile.get_yPos() == _botCoord[1]) {
            if (mazeTile.get_xPos() == _botCoord[0] + 2) {
                moveBot(Constants.right, true);
            } else if (mazeTile.get_xPos() == _botCoord[0] - 2) {
                moveBot(Constants.left, true);
            }
        }
    }

    // helper functions
    private void setBotTiles(int[] _botCoord, int[] _botHeadCoord, boolean updatePrevState) {
        ArrayList<MazeTile> targetedTiles = getTargetTiles(_botCoord[0], _botCoord[1], 0);
        setTile(targetedTiles, Constants.ROBOT_BODY, updatePrevState);
        // forgot what this check is for
        if (_botHeadCoord[0] != _botCoord[0] || _botHeadCoord[1] != _botCoord[1])
            targetedTiles = getTargetTiles(_botHeadCoord[0], _botHeadCoord[1], 3);
            setTile(targetedTiles, Constants.ROBOT_HEAD, false);
    }

    /**
     * updates tile state and re-renders maze
     */
    private void setTile(ArrayList<MazeTile> targetTiles, int newState, boolean updatePrevState) {
        for (MazeTile a : targetTiles) {
            setTile(a, newState, updatePrevState);
        }
    }

    private void setTile(MazeTile a, int newState, boolean updatePrevState) {
        if (newState == Constants.PREV) {
            a.restorePrevState();
        } else {
            a.updateState(newState, updatePrevState);
        }
    }

    /**
     * from a selected tile, get the tiles surrounding it, depending on the mode specified
     * mode: 0 -> Block of 9 tiles
     * mode: 1 -> 3 horizontal tiles
     * mode: 2 -> 3 vertical tiles
     * mode: 3 -> single block
     */
    private ArrayList<MazeTile> getTargetTiles(int centerX, int centerY, int mode) {
        // get surrounding tiles
        int _center = centerX + centerY * MAZE_WIDTH;
        ArrayList<MazeTile> _tempList = new ArrayList<MazeTile>();
        if (mode == 0) {
            _tempList.add(_tileList.get(_center));
            _tempList.add(_tileList.get(_center + 1));
            _tempList.add(_tileList.get(_center - 1));
            _center -= MAZE_WIDTH;
            _tempList.add(_tileList.get(_center));
            _tempList.add(_tileList.get(_center + 1));
            _tempList.add(_tileList.get(_center - 1));
            _center += MAZE_WIDTH * 2;
            _tempList.add(_tileList.get(_center));
            _tempList.add(_tileList.get(_center + 1));
            _tempList.add(_tileList.get(_center - 1));
        } else if (mode == 1) {
            _tempList.add(_tileList.get(_center));
            _tempList.add(_tileList.get(_center + 1));
            _tempList.add(_tileList.get(_center - 1));
        } else if (mode == 2) {
            _tempList.add(_tileList.get(_center));
            _tempList.add(_tileList.get(_center + MAZE_WIDTH));
            _tempList.add(_tileList.get(_center - MAZE_WIDTH));
        } else if (mode == 3) {
            _tempList.add(_tileList.get(_center));
        }
        return _tempList;
    }

    /**
     * Correct the selected tile, say if it is at the maze edge, shift it inwards
     */
    private int[] correctSelectedTile(int centerX, int centerY, int mode) {
        if (mode == 0 || mode == 1) {
            centerX = correctSelectedTileX(centerX);
        }
        if (mode == 0 || mode == 2) {
            centerY = correctSelectedTileY(centerY);
        }
        int[] result = {centerX, centerY};
        return result;
    }

    private int correctSelectedTileX(int xPos) {
        if (xPos == 0) xPos += 1;
        if (xPos == MAZE_WIDTH - 1) xPos -= 1;
        return xPos;
    }

    private int correctSelectedTileY(int yPos) {
        if (yPos == 0) yPos += 1;
        if (yPos == MAZE_HEIGHT - 1) yPos -= 1;
        return yPos;
    }

    /**
     * Required Android function for positioning child views, don't change this
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (TILESIZE == 0) {
            int width = this.getWidth();
            int height = this.getHeight();
            TILESIZE = Math.min(width / MAZE_WIDTH, height / MAZE_HEIGHT);
        }

        int i;
        for (i = 0; i < _tileList.size(); i++) {
            int xPos = i % MAZE_WIDTH * TILESIZE;
            int yPos = i / MAZE_WIDTH * TILESIZE;
            _tileList.get(i).layout(xPos, yPos, xPos + TILESIZE, yPos + TILESIZE);
        }
    }
}