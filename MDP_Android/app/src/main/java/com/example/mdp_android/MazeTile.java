package com.example.mdp_android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import java.util.Random;

public class MazeTile extends View {
    private int _state = 0; // controls tile's appearance

    public MazeTile(Context context){
        super(context);

        // just using random colours for now, to change in the future
        Paint paint = new Paint();
        Random rnd = new Random();
        paint.setARGB(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    /**
     * Required Android function.
     * Draws the tile everytime Android system does a re-render
     * To include in future: different colours/images for different states
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if(_state == 0) {
            Paint paint = new Paint();
            Random rnd = new Random();
            paint.setARGB(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

            Rect rectangle = new Rect(0, 0, Maze.TILESIZE, Maze.TILESIZE);
            canvas.drawRect(rectangle, paint);
        }
        else {
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            Rect rectangle = new Rect(0, 0, Maze.TILESIZE, Maze.TILESIZE);
            canvas.drawRect(rectangle, paint);
        }
    }

    /**
     * Callback for click listener. For testing only, to change in the future.
     */
    public void clicked(){
        //change state
        _state = 1;
        invalidate(); //trigger redraw
    }
}
