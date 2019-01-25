package com.example.mdp_android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupMaze(10,10);
    }

    /**
     * Creates 'maze' container viewgroup and initalizes it with x * y number of tiles
     * @param numTileWidth number of tiles in breadth of maze
     * @param numTileHeight number of tiles in height of maze
     */
    private void setupMaze(int numTileWidth, int numTileHeight){
        RelativeLayout mazeLayout = (RelativeLayout) findViewById(R.id.mazeLayout);
        Maze maze = new Maze(this, numTileWidth, numTileHeight);
        mazeLayout.addView(maze);
    }

    /*
    public int dpToPixel(int dp){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float logicalDensity = metrics.density;
        return (int) Math.ceil(dp * logicalDensity);
    }
    */
}