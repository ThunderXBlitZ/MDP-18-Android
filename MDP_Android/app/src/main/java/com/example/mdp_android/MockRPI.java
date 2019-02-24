package com.example.mdp_android;

import android.os.Handler;
import android.util.Log;

import com.example.mdp_android.tabs.BluetoothFragment;
import com.example.mdp_android.tabs.CommFragment;
import com.example.mdp_android.tabs.MapFragment;

import java.util.Arrays;
import java.util.List;

public class MockRPI {
    private static MockRPI _instance;
    MainActivity _activity;

    private int[] exploreArray = new int[300];
    private int[] obstacleArray = new int[300];

    public MockRPI(MainActivity activity) {
        _activity = activity;
        _instance = this;

        for (int k = 0; k < exploreArray.length; k++) {
            exploreArray[k] = Constants.UNEXPLORED;
            obstacleArray[k] = Constants.EXPLORED;
        }

        for (int j = 45; j < 50; j++){
            obstacleArray[j] = Constants.OBSTACLE;
        }
    }

    public static MockRPI getInstance() {
        return _instance;
    }

    public void receivedEvent(String msg) {
        if (msg.contains("|")) {
            String[] tmp = msg.split("\\|");
            String type = tmp[0];
            String value = tmp.length > 1 ? tmp[1] : "";

            Log.d("receivedMsg", msg);
            Log.d("receivedType", type);
            Log.d("receivedValue", value);
            // switch type, send json message back to relevant fragment
            Log.d("equal", String.valueOf(type.equals("beginExplore")));
            if (type.equals("beginExplore")) {
                int time = 300;

                int botX = 1;
                int botY = 1;

                String result3 = "";
                for (int j = 0; j < obstacleArray.length; j++)
                    result3 += String.valueOf(obstacleArray[j]);
                final String result2 = result3;

                for (int i = 0; i < 13; i++) {
                    String result = "";
                    for (int j = 0; j < exploreArray.length; j++)
                        result += String.valueOf(exploreArray[j]);
                    exploreArray[i] = Constants.EXPLORED;
                    exploreArray[i + 15] = Constants.EXPLORED;
                    exploreArray[i + 30] = Constants.EXPLORED;
                    Handler handler = new Handler();
                    final String tmp2 = result;
                    final String robotPos = "("+botX+++","+botY+")";
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            _activity.notifyFragments(Constants.MESSAGE_READ, "exploreData", tmp2);
                            _activity.notifyFragments(Constants.MESSAGE_READ, "fastestPathData", result2);
                            _activity.notifyFragments(Constants.MESSAGE_READ, "robotPos", robotPos);
                            _activity.notifyFragments(Constants.MESSAGE_READ, "robotDir", "1");
                            _activity.notifyFragments(Constants.MESSAGE_READ, "arrowBlockDown", "200");
                        }
                    }, time);
                    time += 300;
                };

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        _activity.notifyFragments(Constants.MESSAGE_READ, "exploreDone", "");
                    }
                }, 3000);

            }
        } else {
            Log.i("UnknownMsg", msg);
        }
    }
}
