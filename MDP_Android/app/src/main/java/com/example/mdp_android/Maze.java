package com.example.mdp_android;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Random;

public class Maze extends ViewGroup {
    private int _xIndex;
    private int _yIndex;
    private ArrayList<MazeTile> _tileList;
    public static int TILESIZE;

    public Maze(Context context, int xIndex, int yIndex) {
        super(context);
        _xIndex = xIndex;
        _yIndex = yIndex;
        TILESIZE = getScreenWidth()/_xIndex;

        _tileList = new ArrayList<MazeTile>(_xIndex*_yIndex);

        int i,j;
        for (i=0; i < _yIndex; i++){
            for(j = 0; j < _xIndex; j++){
                MazeTile mazeTile = new MazeTile(context);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(TILESIZE, TILESIZE);
                params.leftMargin = i*TILESIZE;
                params.topMargin = j*TILESIZE;
                this.addView(mazeTile, params);
                _tileList.add(mazeTile);

                mazeTile.setOnClickListener(tileListener);
            }
        }
    }

    /**
     * Position all children within this layout.
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        for(int i=0; i < _tileList.size(); i++){
            int xPos = Math.round(i%_xIndex) * TILESIZE;
            int yPos = Math.round(i/_yIndex) * TILESIZE;
            _tileList.get(i).layout(xPos, yPos, xPos + TILESIZE, yPos + TILESIZE);
        }
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    private View.OnClickListener tileListener = new View.OnClickListener ()
    {
        public void onClick(View v)
        {
            if(v instanceof MazeTile){
                MazeTile mazeTile = (MazeTile) v;
                mazeTile.clicked();
            }
            // more for other UI buttons
        }
    };
}