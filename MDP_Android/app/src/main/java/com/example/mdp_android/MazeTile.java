package com.example.mdp_android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

import java.util.HashMap;

public class MazeTile extends View {
    private static int UNEXPLORED;
    private static int EXPLORED;
    private static int OBSTACLE;
    private static int START;
    private static int GOAL;
    private static int WAYPOINT;
    private static int ROBOT_HEAD;
    private static int ROBOT_BODY;
    private static final int red = Color.RED;
    private static HashMap<Integer, Integer> colorMap = null;

    private int _state = Constants.UNEXPLORED; // controls tile's appearance
    private int _xPos = -1;
    private int _yPos = -1;

    public MazeTile(Context context, int XPos, int YPos){
        super(context);
        _xPos = XPos;
        _yPos = YPos;

        if (colorMap == null){
            UNEXPLORED = 0xFFA9A9A9; // light grey 0xFFD3D3D3; // Color.GRAY;
            EXPLORED = Color.WHITE;
            OBSTACLE = Color.BLACK;
            ROBOT_BODY = getResources().getColor(R.color.pink);
            ROBOT_HEAD = getResources().getColor(R.color.colorPrimaryDark);
            START = getResources().getColor(R.color.orange);
            WAYPOINT = getResources().getColor(R.color.yellow); // 0xFF96deff;
            GOAL = getResources().getColor(R.color.green);

            colorMap = new HashMap<Integer, Integer>();
            colorMap.put(Constants.UNEXPLORED, UNEXPLORED);
            colorMap.put(Constants.EXPLORED, EXPLORED);
            colorMap.put(Constants.OBSTACLE, OBSTACLE);
            colorMap.put(Constants.START, START);
            colorMap.put(Constants.GOAL, GOAL);
            colorMap.put(Constants.WAYPOINT, WAYPOINT);
            colorMap.put(Constants.ROBOT_HEAD, ROBOT_HEAD);
            colorMap.put(Constants.ROBOT_BODY, ROBOT_BODY);
            colorMap.put(999, red);
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
        if (_state >= Constants.UNEXPLORED && _state <= Constants.OBSTACLE /*|| _state ==999*/) {
            Rect rectangle = new Rect(0, 0, Maze.TILESIZE-Constants.tilePadding, Maze.TILESIZE-Constants.tilePadding);
            Paint paint = new Paint();
            paint.setColor(colorMap.get(_state));
            canvas.drawRect(rectangle, paint);
        }

        // red
        else if (_state >= Constants.NORTH && _state <= Constants.WEST){
            Bitmap bitmap = null;
            bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.up_arrow_foreground);
            /* only up arrows are tested
            switch (_state){
                case Constants.NORTH:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.up_arrow_foreground);
                    break;
                case Constants.SOUTH:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.down_arrow_foreground);
                    break;
                case Constants.EAST:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.left_arrow_foreground);
                    break;
                case Constants.WEST:
                    default:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.right_arrow_foreground);
                    break;
            }
            */
            canvas.drawBitmap(bitmap, null, new RectF(0, 0, Maze.TILESIZE-Constants.tilePadding, Maze.TILESIZE-Constants.tilePadding), null);
        }
    }

    public int getState(){
        return _state;
    }

    public void setState(int newState){
        // if(newState != _state){
            _state = newState;
            invalidate();
        // }
    }

    public int get_xPos(){
        return _xPos;
    }

    public int get_yPos(){
        return _yPos;
    }

    public void reset(){
        _state = Constants.UNEXPLORED;
        invalidate();
    }
}
