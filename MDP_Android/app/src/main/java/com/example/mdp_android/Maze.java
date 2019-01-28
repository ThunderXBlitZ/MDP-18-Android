package com.example.mdp_android;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class Maze extends ViewGroup {
    private int _xIndex;
    private int _yIndex;
    private ArrayList<MazeTile> _tileList;
    public static int TILESIZE;

    /**
     * Constructor for maze. Creates x * y number of tiles and stores in arrayList '_tileList'
     * @param context
     * @param xIndex
     * @param yIndex
     */
    public Maze(Context context, int xIndex, int yIndex) {
        super(context);
        _xIndex = xIndex;
        _yIndex = yIndex;
        TILESIZE = Util.getScreenWidth()/_xIndex;

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

                mazeTile.setOnClickListener(tileListener); // for testing only
            }
        }
    }

    //click listener for testing, to change in the future
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

    /**
     * Required Android function, dont change this
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        for(int i=0; i < _tileList.size(); i++){
            int xPos = Math.round(i%_xIndex) * TILESIZE;
            int yPos = Math.round(i/_yIndex) * TILESIZE;
            _tileList.get(i).layout(xPos, yPos, xPos + TILESIZE, yPos + TILESIZE);
        }
    }
}