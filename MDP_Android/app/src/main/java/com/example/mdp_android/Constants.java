/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.mdp_android;

import com.example.mdp_android.bluetooth.BluetoothChatService;

/**
 * Defines several constants used between {@link BluetoothChatService} and the UI.
 */
public interface Constants {

    // Message types sent from the BluetoothChatService Handler
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_DEVICE_ADDRESS = 5;
    int MESSAGE_TOAST = 6;

    // Key names received from the BluetoothChatService Handler
    String DEVICE_NAME = "device_name";
    String DEVICE_ADDRESS = "device_address";
    String TOAST = "toast";

    String PREF_NAME  = "custom_write_text";

    // mazeTile states
    int UNEXPLORED = 0;
    int EXPLORED = 1;
    int OBSTACLE = 2;
    int START = 3;
    int GOAL = 4;
    int WAYPOINT = 5;
    int PREV = 6;

    int ROBOT_HEAD = 7;
    int ROBOT_BODY = 8;

    int NORTH = 9;
    int EAST = 10;
    int SOUTH = 11;
    int WEST = 12;

    // mapFragment input states
    int idleMode = -1;
    int coordinateMode = 0;
    int waypointMode = 1;
    int exploreMode = 2;
    int fastestPathMode = 4;
    int manualMode = 5;

    int up = 0;
    int down = 1;
    int left = 2;
    int right = 3;

}
