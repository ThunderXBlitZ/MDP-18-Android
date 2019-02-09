package com.example.mdp_android;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class Maze extends ViewGroup {
    private int _numWidth;
    private int _numHeight;
    private ArrayList<MazeTile> _tileList;
    public static int TILESIZE = 0;

    /**
     * Constructor for maze. Creates x * y number of tiles and stores in arrayList '_tileList'
     * @param context
     * @param numWidth
     * @param numHeight
     */
    public Maze(Context context, int numWidth, int numHeight) {
        super(context);
        _numWidth = numWidth;
        _numHeight = numHeight;
        _tileList = new ArrayList<MazeTile>(_numWidth*_numHeight);

        int i,j;
        for (i=0; i < _numWidth; i++){
            for(j = 0; j < _numHeight; j++){
                MazeTile mazeTile = new MazeTile(context);
                this.addView(mazeTile);
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
        if(TILESIZE == 0){
            int width = this.getWidth();
            int height = this.getHeight();
            TILESIZE = Math.min(width/_numWidth, height/_numHeight);
        }

        int i;
        for (i=0; i < _tileList.size(); i++){
            int xPos = i%_numWidth*TILESIZE;
            int yPos = i/_numWidth*TILESIZE;
            _tileList.get(i).layout(xPos, yPos, xPos + TILESIZE, yPos + TILESIZE);
            }
        }
}