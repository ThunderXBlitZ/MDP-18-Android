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

import java.util.ArrayList;

public class Maze extends ViewGroup {
    public static int TILESIZE = 0;
    private static final int MAZE_WIDTH = 15;
    private static final int MAZE_HEIGHT = 20;
    private ArrayList<MazeTile> _tileList;
    private int _inputState = 0;
    private Boolean _coordinatesSet = false;
    private Boolean _exploreCompleted = false;

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

                mazeTile.setOnClickListener(_tileListener); // for testing only
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

    public void moveBot(int direction, Boolean isExplore) {
        int[] _botHeadCoord = _botCoord.clone();

        if (direction == Constants.left && _botCoord[0] > 1) {
            clearBot(isExplore);
            _botCoord[0] -= 1;
            _botHeadCoord[0] = _botCoord[0] - 1;
            updateBotTiles(_botCoord, _botHeadCoord, false);
        } else if (direction == Constants.up && _botCoord[1] > 1) {
            clearBot(isExplore);
            _botCoord[1] -= 1;
            _botHeadCoord[1] = _botCoord[1] - 1;
            updateBotTiles(_botCoord, _botHeadCoord, false);
        } else if (direction == Constants.right && _botCoord[0] < MAZE_WIDTH - 2) {
            clearBot(isExplore);
            _botCoord[0] += 1;
            _botHeadCoord[0] = _botCoord[0] + 1;
            updateBotTiles(_botCoord, _botHeadCoord, false);
        } else if (direction == Constants.down && _botCoord[1] < MAZE_HEIGHT - 2) {
            clearBot(isExplore);
            _botCoord[1] += 1;
            _botHeadCoord[1] = _botCoord[1] + 1;
            updateBotTiles(_botCoord, _botHeadCoord, false);
        } else if (direction == -1) { // for start position, face north
            _botHeadCoord[1] = _botCoord[1] - 1;
            updateBotTiles(_botCoord, _botHeadCoord, true);
        }
    }

    private void clearBot(Boolean isExplore){
        if(isExplore){
            setTileState(Constants.EXPLORED, _botCoord[0], _botCoord[1], 0, false);
        } else {
            setTileState(Constants.PREV, _botCoord[0], _botCoord[1], 0, false);
        }
    }

    private void updateBotTiles(int[] _botCoord, int[] _botHeadCoord, boolean updatePrevState){
        setTileState(Constants.ROBOT_BODY, _botCoord[0], _botCoord[1], 0, updatePrevState);
        if(_botHeadCoord[0] != _botCoord[0] || _botHeadCoord[1] != _botCoord[1]) setTileState(Constants.ROBOT_HEAD, _botHeadCoord[0], _botHeadCoord[1], 3, false);
    }

    public void explore(){
        // do exploration
        for (MazeTile a:_tileList){
            if(a.getState() != Constants.ROBOT_BODY && a.getState() != Constants.ROBOT_HEAD && a.getState() != Constants.EXPLORED){
                a.forceUpdatePrevState();
            }
        }
        _exploreCompleted = true;
        for (MazeTile a:_tileList){
            if(a.getState() == Constants.EXPLORED) a.restorePrevState();
        }
    }

    public void fastestPath(){
        // do fastestPath
    }

    public void clearWaypoints(){
        for( Integer[] a:_waypointList){
            setTileState(Constants.UNEXPLORED, a[0], a[1], 0, false);
        }
        _waypointList = new ArrayList<Integer[]>();
    }

    public void reset(){
        _inputState = Constants.idleMode;
        _coordCount = 0;
        _coordinatesSet = false;
        _exploreCompleted = false;
        clearWaypoints();
        for (MazeTile i:_tileList){
            i.reset();
        }
    }

    // updates tile state and re-renders maze
    // mode: 0 -> Block of 9 tiles. 1 -> 3 horizontal tiles. 2 -> 3 vertical tiles. 3 -> single block
    private int[] setTileState(int newState, int centerX, int centerY, int mode, boolean updatePrevState){
        if(mode == 0 || mode == 1){
            centerX = correctSelectedTileX(centerX);
        }
        if(mode == 0 || mode == 2){
            centerY = correctSelectedTileY(centerY);
        }
        // update tiles states
        int _center = centerX+centerY*MAZE_WIDTH;
        ArrayList<MazeTile> _tempList = new ArrayList<MazeTile>();
        if(mode == 0){
            _tempList.add(_tileList.get(_center));
            _tempList.add(_tileList.get(_center+1));
            _tempList.add(_tileList.get(_center-1));
            _center -= MAZE_WIDTH;
            _tempList.add(_tileList.get(_center));
            _tempList.add(_tileList.get(_center+1));
            _tempList.add(_tileList.get(_center-1));
            _center += MAZE_WIDTH * 2;
            _tempList.add(_tileList.get(_center));
            _tempList.add(_tileList.get(_center+1));
            _tempList.add(_tileList.get(_center-1));
        } else if(mode == 1){
            _tempList.add(_tileList.get(_center));
            _tempList.add(_tileList.get(_center+1));
            _tempList.add(_tileList.get(_center-1));
        } else if(mode == 2){
            _tempList.add(_tileList.get(_center));
            _tempList.add(_tileList.get(_center+MAZE_WIDTH));
            _tempList.add(_tileList.get(_center-MAZE_WIDTH));
        } else if(mode == 3){
            _tempList.add(_tileList.get(_center));
        }
        for (MazeTile a:_tempList){
            if(newState == Constants.PREV){
                a.restorePrevState();
            } else {
                a.updateState(newState, updatePrevState);
            }
        }
        _tempList = null;
        int[] result = { centerX, centerY };
        return result;
    }

    private int[] _botCoord = {0, 0};
    private int[] _startCoord = {0, 0};
    private int[] _endCoord = {0, 0};
    private int _coordCount = 0;
    private ArrayList<Integer[]> _waypointList = new ArrayList<Integer[]>();
    private View.OnClickListener _tileListener = new View.OnClickListener ()
    {
        public void onClick(View v)
        {
            if(v instanceof MazeTile){
                MazeTile mazeTile = (MazeTile) v;

                if(_inputState == Constants.coordinateMode){
                    if (_coordCount == 0){
                        _coordinatesSet = false;
                        // clear previously set start/end tiles
                        setTileState(Constants.UNEXPLORED, _startCoord[0], _startCoord[1], 0, false);
                        setTileState(Constants.UNEXPLORED, _endCoord[0], _endCoord[1], 0, false);
                        // set start tiles
                        _startCoord = setTileState(Constants.START, mazeTile.get_xPos(), mazeTile.get_yPos(), 0, true);
                        _botCoord = _startCoord;
                        moveBot(-1, false); // set bot at start coordinates
                        _coordCount = 1;
                    } else if (_coordCount == 1){
                        _endCoord = setTileState(Constants.GOAL, mazeTile.get_xPos(), mazeTile.get_yPos(), 0, true);
                        _coordCount = 0;
                        _coordinatesSet = true;
                    }
                } else if (_inputState == Constants.waypointMode){
                    int[] waypoint = setTileState(Constants.WAYPOINT, mazeTile.get_xPos(), mazeTile.get_yPos(), 0, true);
                    Integer[] waypoint2 = {(Integer) waypoint[0], (Integer) waypoint[1]};
                    _waypointList.add(waypoint2);
                } else if (_inputState == Constants.manualMode){
                    if (mazeTile.get_xPos() == _botCoord[0]){
                        if(mazeTile.get_yPos() == _botCoord[1]+2){
                            moveBot(Constants.down, true);
                        } else if(mazeTile.get_yPos() == _botCoord[1]-2){
                            moveBot(Constants.up, true);
                        }
                    } else if (mazeTile.get_yPos() == _botCoord[1]){
                        if(mazeTile.get_xPos() == _botCoord[0]+2){
                            moveBot(Constants.right, true);
                        } else if(mazeTile.get_xPos() == _botCoord[0]-2){
                            moveBot(Constants.left, true);
                        }
                    }
                }
                /// mazeTile.clicked();
            }
        }
    };

    private int correctSelectedTileX(int xPos){
        if(xPos == 0) xPos += 1;
        if(xPos == MAZE_WIDTH-1) xPos -= 1;
        return xPos;
    }

    private int correctSelectedTileY(int yPos){
        if(yPos == 0) yPos += 1;
        if(yPos == MAZE_HEIGHT-1) yPos -= 1;
        return yPos;
    }
    /**
     * Required Android function for positioning child views, don't change this
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if(TILESIZE == 0){
            int width = this.getWidth();
            int height = this.getHeight();
            TILESIZE = Math.min(width/MAZE_WIDTH, height/MAZE_HEIGHT);
        }

        int i;
        for (i=0; i < _tileList.size(); i++){
            int xPos = i%MAZE_WIDTH*TILESIZE;
            int yPos = i/MAZE_WIDTH*TILESIZE;
            _tileList.get(i).layout(xPos, yPos, xPos + TILESIZE, yPos + TILESIZE);
            }
        }
}