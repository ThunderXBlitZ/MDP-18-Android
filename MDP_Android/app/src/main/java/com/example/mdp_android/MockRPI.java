package com.example.mdp_android;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

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
        testBufferedInput("2222;");
        testBufferedInput("abc|123456;");
        testBufferedInput("cde|;");
        testBufferedInput("fgh|999;ijk|");
        testBufferedInput("12345");
        testBufferedInput("67890;aa");
        testBufferedInput("|aaaa;11|");
        testBufferedInput("0000;00|asd");
        testBufferedInput("12345;1111;");
        testBufferedInput("2222;");
    }

    public static MockRPI getInstance() {
        return _instance;
    }

    private String _storedMessage = "";
    public void testBufferedInput(String readMessage){
        if(readMessage.contains(";")) {
            String[] msgList = readMessage.split(";");
                for (int i = 0; i < msgList.length; i++) {
                    String processedMsg;
                    if (i == 0) {
                        processedMsg = _storedMessage + msgList[i];
                        _storedMessage = "";
                    } else if (i == msgList.length - 1) {
                        if(readMessage.charAt(readMessage.length()-1) == ';'){
                            processedMsg = msgList[i];
                        } else {
                            _storedMessage += msgList[i];
                            continue;
                        }
                    } else {
                        processedMsg = msgList[i];
                    }
                    String type = null;
                    String value = processedMsg;
                    if (value != null && value.contains("|")) {
                        String[] tmp = value.split("|");
                        type = tmp[0] != "" ? tmp[0] : "";
                        value = tmp[1] != "" ? tmp[1] : "";
                    }
                    Log.d("rawMsgReceived", processedMsg);
                    // notifyFragments(Constants.MESSAGE_READ, type,  value);
                }

        } else {
            _storedMessage += readMessage;
        }
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
                int botY = 18;

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
                            _activity.notifyFragments(Constants.MESSAGE_READ, "exploreData", "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
                            _activity.notifyFragments(Constants.MESSAGE_READ, "fastestPathData", "0000020008002000800000001f8000200040008438098010002000400880f00000000000080");
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
