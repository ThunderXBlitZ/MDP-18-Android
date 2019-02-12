package com.example.mdp_android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import java.util.HashMap;
import java.util.Random;

public class MazeTile extends View {
    private static final int UNEXPLORED = Color.BLUE;
    private static final int EXPLORED = Color.GREEN;
    private static final int OBSTACLE = Color.BLACK;
    private static final int START = Color.YELLOW;
    private static final int GOAL = Color.MAGENTA;
    private static final int WAYPOINT = Color.CYAN;
    private static final int ROBOT_HEAD = Color.DKGRAY;
    private static final int ROBOT_BODY = Color.RED;
    private static HashMap<Integer, Integer> colorMap = null;

    private int _state = Constants.UNEXPLORED; // controls tile's appearance
    private int _prevState = Constants.UNEXPLORED;
    private int _xPos = -1;
    private int _yPos = -1;

    public MazeTile(Context context, int XPos, int YPos){
        super(context);
        _xPos = XPos;
        _yPos = YPos;

        if (colorMap == null){
            colorMap = new HashMap<Integer, Integer>();
            colorMap.put(Constants.UNEXPLORED, UNEXPLORED);
            colorMap.put(Constants.EXPLORED, EXPLORED);
            colorMap.put(Constants.OBSTACLE, OBSTACLE);
            colorMap.put(Constants.START, START);
            colorMap.put(Constants.GOAL, GOAL);
            colorMap.put(Constants.WAYPOINT, WAYPOINT);
            colorMap.put(Constants.ROBOT_HEAD, ROBOT_HEAD);
            colorMap.put(Constants.ROBOT_BODY, ROBOT_BODY);
        }
    }

    /**
     * Required Android function.
     * Draws the tile everytime Android system does a re-render
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        /* TBD: for directional blocks, use images
        if(_state == Constants.NORTH) {
            return
        } */
        if (_state >= Constants.UNEXPLORED && _state <= Constants.ROBOT_BODY) {
            Rect rectangle = new Rect(0, 0, Maze.TILESIZE, Maze.TILESIZE);
            Paint paint = new Paint();
            paint.setColor(colorMap.get(_state));
            canvas.drawRect(rectangle, paint);
        }
    }

    public int getState(){
        return _state;
    }
    public void forceUpdatePrevState(){
        _prevState = _state;
    }

    public void updateState(int newState, boolean setPrevState){
        if(setPrevState) _prevState = _state;
        _state = newState;
        invalidate();
    }

    public void restorePrevState(){
        _state = _prevState;
        invalidate();
    }
    /**
     * Callback for click listener. For testing only, to change in the future.
     */
    public void clicked(){
        //change state
        _state = 1;
        invalidate(); //trigger redraw
    }

    public int get_xPos(){
        return _xPos;
    }

    public int get_yPos(){
        return _yPos;
    }

    public void reset(){
        _state = Constants.UNEXPLORED;
        _prevState = _state;
        invalidate();
    }
}
