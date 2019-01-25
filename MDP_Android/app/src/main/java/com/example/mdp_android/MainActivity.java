package com.example.mdp_android;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RelativeLayout mazeLayout = (RelativeLayout) findViewById(R.id.mazeLayout);
        setupMaze(10,10);
    }

    private void setupMaze(int numTileWidth, int numTileHeight){
        RelativeLayout mazeLayout = (RelativeLayout) findViewById(R.id.mazeLayout);
        Maze maze = new Maze(this, numTileWidth, numTileHeight);
        mazeLayout.addView(maze);
    }

    public int dpToPixel(int dp){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float logicalDensity = metrics.density;
        return (int) Math.ceil(dp * logicalDensity);
    }
}