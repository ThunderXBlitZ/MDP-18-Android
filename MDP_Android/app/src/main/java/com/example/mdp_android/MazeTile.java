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

    private int _state = 0;

    public MazeTile(Context context){
        super(context);
        Paint paint = new Paint();
        Random rnd = new Random();
        paint.setARGB(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

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

    public void clicked(){
        //change state
        _state = 1;
        invalidate(); //trigger redraw
    }
}
