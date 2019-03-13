package com.example.mdp_android;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.example.mdp_android.bluetooth.BluetoothManager;

import java.util.ArrayList;


public class Maze extends ViewGroup {
    // maze constants
    private static final int MAZE_WIDTH = 15;
    private static final int MAZE_HEIGHT = 20;
    public static int TILESIZE = 0;
    private final int[] _emptyArray = new int[MAZE_HEIGHT * MAZE_WIDTH];

    // maze entities
    private ArrayList<MazeTile> _tileList;

    // maze data
    private int[] _botCoord = {0, 0};
    private int[] _headCoord = {0, 0};
    private int _direction = Constants.NORTH;
    private int[] _startCoord = {0, 0};
    private int[] _endCoord = {0, 0};
    private int[] _wpCoord = {-1, -1};
    private ArrayList<Integer[]> _arrowBlockList = new ArrayList<Integer[]>();
    private int[] _obstacleData = new int[MAZE_HEIGHT * MAZE_WIDTH];
    private int[] _exploreData = new int[MAZE_HEIGHT * MAZE_WIDTH];

    // managing input states
    private int _coordCount = -1;
    private boolean _wpSet = false;
    private int _inputState = Constants.idleMode;
    private Boolean _exploreCompleted = false;

    /**
     * Constructor for maze. Creates 15 * 20 number of tiles and stores in arrayList '_tileList'
     *
     * @param context
     */
    public Maze(Context context) {
        super(context);
        _tileList = new ArrayList<MazeTile>(MAZE_WIDTH * MAZE_HEIGHT);

        // generate mazeTiles, save to arraylist
        int i, j;
        for (i = 0; i < MAZE_HEIGHT; i++) {
            for (j = 0; j < MAZE_WIDTH; j++) {
                MazeTile mazeTile = new MazeTile(context, j, i);
                this.addView(mazeTile);
                _tileList.add(mazeTile);
                mazeTile.setOnClickListener(_tileListener);
            }
        }

        for (int k = 0; k < MAZE_WIDTH * MAZE_HEIGHT; k++) {
            _emptyArray[k] = Constants.UNEXPLORED;
        }
        reset();
    }

    public int getState() {
        return _inputState;
    }

    public void setState(int newState) {
        _inputState = newState;
    }

    public boolean coordinatesSet() {
        return _coordCount == 1;
    }

    public boolean isExploreCompleted() {
        return _exploreCompleted;
    }

    public void setExploreCompleted(Boolean value) {
        _exploreCompleted = value;
    }

    public void handleExplore(String binaryData) {
        // binaryData is Maze Size 300/4 = 75 hex characters
        // for explored data, algo requirement is to pad first and last 2 bits with 0s
        // so we will drop those
        String tmp = parseHexCharToBinary(binaryData);
        _exploreData = convertStrToIntArray(tmp.substring(2, Math.max(tmp.length(), MAZE_HEIGHT * MAZE_WIDTH) - 2));
        renderMaze();
    }

    public void handleObstacle(String binaryData) {
        // obstacle data is mapped to EXPLORED tiles in _exploredData
        String tmp = parseHexCharToBinary(binaryData);
        int count = 0;
        int mazeSize = _exploreData.length;
        int[] result = _emptyArray.clone();
        for (int j = 0; j < tmp.length(); j++) {
            int myChar = Character.getNumericValue(tmp.charAt(j));
            while (count < mazeSize && _exploreData[count] == Constants.UNEXPLORED) count++;
            if(count >= mazeSize) break;
            result[count] = myChar;
            count++;
        }
        _obstacleData = result;
        renderMaze();
    }

    // amd tool only
    public void handleAMDGrid(String binaryData) {
        _exploreData = convertStrToIntArray(parseHexCharToBinary("fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"));
        _obstacleData = convertStrToIntArray(parseHexCharToBinary(binaryData));
        renderMaze();
    }

    public void updateBotPosDir(String data) {
        try {
            String tmp[] = data.split(",");
            if (tmp.length == 3) {
                String dir = tmp[2];
                int yPos = Integer.parseInt(tmp[0]);
                int xPos = Integer.parseInt(tmp[1]);
                _direction = convertDirStrToNum(dir);
                _botCoord[0] = xPos;
                _botCoord[1] = yPos;
                renderMaze();
            }
        } catch (NumberFormatException e) {
            Log.e("robotPos", e.getMessage());
        }
    }

    public void resetWp() {
        _wpCoord[0] = -1;
        _wpCoord[1] = -1;
        _wpSet = false;
        renderMaze();
    }

    public void resetStartEnd() {
        _coordCount = -1;
        renderMaze();
    }

    public void reset() {
        _obstacleData = _emptyArray.clone();
        _exploreData = _emptyArray.clone();
        _arrowBlockList = new ArrayList<Integer[]>();
        resetWp();
        _inputState = Constants.idleMode;
        resetStartEnd();
        _exploreCompleted = false;
        _direction = Constants.NORTH;
        for (MazeTile i : _tileList) {
            i.reset();
        }
        renderMaze();
    }

    /**
     * Data from algo is 4 characters of 0/1 is converted to 1 hex char
     */
    private String parseHexCharToBinary(String hexStr) {
        String fullString = "";
        for (int i = 0; i < hexStr.length(); i++) {
            String hexChar = Character.toString(hexStr.charAt(i));
            int hexValue = 0;
            try {
                hexValue = Integer.parseInt(hexChar, 16);
            } catch (NumberFormatException e) {
                Log.e("ParseHexChar", e.getMessage());
            }
            String binary = String.format("%4s", Integer.toString(hexValue, 2)).replace(' ', '0');
            fullString += binary;
        }
        return fullString;
    }

    private int[] convertStrToIntArray(String data) {
        int[] result = new int[data.length()];
        for (int j = 0; j < data.length(); j++) {
            result[j] = Character.getNumericValue(data.charAt(j));
        }
        return result;
    }

    private void printIntArrayAsString(int[] data){
        String tmp = "";
        for (int i=0; i < data.length; i++){
            tmp += data[i];
        }
        Log.d("comms_result", tmp);
    }


    public void handleArrowBlock(int type, String arrowSizeStr) {
        if (!coordinatesSet()) return;

        int distBlock = 1;
        float arrowSize = 0;

        //from our experiments: rpi size: 4.7-> 2 block, 12 -> 3 blocks
        try {
            arrowSize = Float.valueOf(arrowSizeStr);
        } catch (Exception e) {
            Log.e("arrowBlockPos", e.getMessage());
        }

        if (arrowSize <= 10.3 && arrowSize >= 9.8) distBlock = 3;
        else if (arrowSize >= 5.2 && arrowSize <= 6.7) distBlock = 2;
        else return;

        Integer[] blockCoord = new Integer[3];
        blockCoord[2] = _direction;

        if (_direction == Constants.NORTH) {
            blockCoord[0] = _botCoord[0];
            blockCoord[1] = _botCoord[1];
            blockCoord[1] += 1 + distBlock;
        } else if (_direction == Constants.SOUTH) {
            blockCoord[0] = _botCoord[0];
            blockCoord[1] = _botCoord[1];
            blockCoord[1] -= 1 + distBlock;
        } else if (_direction == Constants.EAST) {
            blockCoord[0] = _botCoord[0];
            blockCoord[1] = _botCoord[1];
            blockCoord[0] += 1 + distBlock;
        } else if (_direction == Constants.WEST) {
            blockCoord[0] = _botCoord[0];
            blockCoord[1] = _botCoord[1];
            blockCoord[0] -= 1 + distBlock;
        }

        Log.e("ArrowBlock", _botCoord[0] + " " + _botCoord[1]);
        if (blockCoord[0] < 0 || blockCoord[0] >= MAZE_WIDTH || blockCoord[1] < 0 || blockCoord[1] >= MAZE_HEIGHT)
            return;
        _arrowBlockList.add(blockCoord);
        displayArrowBlockString(blockCoord[0], blockCoord[1], blockCoord[2]);
        renderMaze();
    }

    private void displayArrowBlockString(int x, int y, int dir) {
        // invert bot's dir to get direction arrow block is facing relative to start point
        String blockDir = "";
        if (dir == Constants.NORTH) blockDir = "D";
        else if (dir == Constants.SOUTH) blockDir = "U";
        else if (dir == Constants.EAST) blockDir = "L";
        else if (dir == Constants.WEST) blockDir = "R";
        MainActivity.updateMsgHistory("Arrow Block detected at: x:" + x + " y:" + y + ", facing: " + blockDir);
    }

    private View.OnClickListener _tileListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (v instanceof MazeTile) {
                MazeTile mazeTile = (MazeTile) v;
                if (_inputState == Constants.coordinateMode) {
                    handleCoordinatesInput(mazeTile);
                } else if (_inputState == Constants.waypointMode) {
                    handleWaypointInput(mazeTile);
                } else if (_inputState == Constants.manualMode) {
                    handleManualInput(mazeTile);
                }
            }
        }
    };

    private void handleCoordinatesInput(MazeTile mazeTile) {
        // havent set any start coordinates, or already set.
        if (_coordCount == -1 || _coordCount == 1) {
            _startCoord = correctSelectedTile(mazeTile.get_xPos(), mazeTile.get_yPos(), 0);
            _botCoord = _startCoord.clone();
            _coordCount = 0;
            _direction = Constants.NORTH;
            BluetoothManager.getInstance().sendMessage("START_POS", _startCoord[0] + "," + _startCoord[1]);
        }
        // set end coordinates
        else if (_coordCount == 0) {
            _endCoord = correctSelectedTile(mazeTile.get_xPos(), mazeTile.get_yPos(), 0);
            _coordCount = 1;
            BluetoothManager.getInstance().sendMessage("END_POS", _endCoord[0] + "," + _endCoord[1]);

            // clean up
            setState(Constants.idleMode);
        }
        renderMaze();
    }

    private void handleWaypointInput(MazeTile mazeTile) {
        int[] waypointCoord = correctSelectedTile(mazeTile.get_xPos(), mazeTile.get_yPos(), 0);
        ArrayList<MazeTile> targetMazeTiles = getTargetTiles(waypointCoord[0], waypointCoord[1], 0);

        // check that target tiles are not occupied by obstacle or arrow block
        for (MazeTile a : targetMazeTiles) {
            if (isObstacle(a)) return;
        }
        _wpCoord = waypointCoord;
        _wpSet = true;
        renderMaze();
        BluetoothManager.getInstance().sendMessage("WP", _wpCoord[0] + "," + _wpCoord[1]);

        // clean up
        setState(Constants.idleMode);
    }

    private void handleManualInput(MazeTile mazeTile) {
        if (mazeTile.get_xPos() == _botCoord[0]) {
            if (mazeTile.get_yPos() == _botCoord[1] + 2) {
                attemptMoveBot(Constants.NORTH, mazeTile);
            } else if (mazeTile.get_yPos() == _botCoord[1] - 2) {
                attemptMoveBot(Constants.SOUTH, mazeTile);
            }
        } else if (mazeTile.get_yPos() == _botCoord[1]) {
            if (mazeTile.get_xPos() == _botCoord[0] + 2) {
                attemptMoveBot(Constants.EAST, mazeTile);
            } else if (mazeTile.get_xPos() == _botCoord[0] - 2) {
                attemptMoveBot(Constants.WEST, mazeTile);
            }
        }
    }

    public void attemptMoveBot(int dir) {
        attemptMoveBot(dir, null);
    }

    public void attemptMoveBot(int dir, MazeTile mazeTile) {
        if (canMove(dir, mazeTile)) {
            // amd only. Note: amd bot does not rotate
            /*
            if(dir == Constants.NORTH) BluetoothManager.getInstance().sendMessage("MOVE", "F");
            if(dir == Constants.SOUTH) BluetoothManager.getInstance().sendMessage("MOVE", "B");
            if(dir == Constants.EAST) BluetoothManager.getInstance().sendMessage("MOVE", "SR");
            if(dir == Constants.WEST) BluetoothManager.getInstance().sendMessage("MOVE", "SL");
            */

            if (dir != _direction) {
                int diff = _direction - dir;
                if (Math.abs(diff) == 2) { // opposite direction
                    BluetoothManager.getInstance().sendMessage(null, "L");
                    BluetoothManager.getInstance().sendMessage(null, "L");
                    BluetoothManager.getInstance().sendMessage("SET_STATUS", "Rotating left...");
                } else if (diff == 1 || dir == Constants.WEST && _direction == Constants.NORTH) {
                    BluetoothManager.getInstance().sendMessage(null, "L");
                    BluetoothManager.getInstance().sendMessage("SET_STATUS", "Rotating left...");
                } else if (diff == -1 || dir == Constants.NORTH && _direction == Constants.WEST) {
                    BluetoothManager.getInstance().sendMessage(null, "R");
                    BluetoothManager.getInstance().sendMessage("SET_STATUS", "Rotating right...");
                }
            }
            BluetoothManager.getInstance().sendMessage(null, "F");
            BluetoothManager.getInstance().sendMessage("SET_STATUS", "Moving forward...");
            moveBot(dir);
        }
    }

    private Boolean canMove(int dir, MazeTile mazeTile) {
        int newX = _botCoord[0];
        int newY = _botCoord[1];
        if (mazeTile == null) { // from directional button
            // calc new bot head position
            if (dir == Constants.WEST) {
                newX -= 2;
            } else if (dir == Constants.EAST) {
                newX += 2;
            } else if (dir == Constants.NORTH) {
                newY += 2;
            } else if (dir == Constants.SOUTH) {
                newY -= 2;
            }
            // check if new spot for bot's head is within maze
            if (newY < 0 || newY >= MAZE_HEIGHT || newX < 0 || newX >= MAZE_WIDTH) {
                return false;
            }
        } else {
            newX = mazeTile.get_xPos();
            newY = mazeTile.get_yPos();
        }

        // get new maze tiles for bot head and check if obstacle
        ArrayList<MazeTile> list;
        if (dir == Constants.SOUTH || dir == Constants.NORTH) {
            list = getTargetTiles(newX, newY, 1);
        } else {
            list = getTargetTiles(newX, newY, 2);
        }
        for (MazeTile a : list) {
            if (isObstacle(a)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Only for manual mode
     * Always check whether the bot can move in the specified direction using canMove() first
     */
    private void moveBot(int dir) {
        if (dir == Constants.WEST) {
            _botCoord[0] -= 1;
            _direction = Constants.WEST;
        } else if (dir == Constants.EAST) {
            _botCoord[0] += 1;
            _direction = Constants.EAST;
        } else if (dir == Constants.NORTH) {
            _botCoord[1] += 1;
            _direction = Constants.NORTH;
        } else if (dir == Constants.SOUTH) {
            _botCoord[1] -= 1;
            _direction = Constants.SOUTH;
        }
        renderMaze();
    }

    private void updateBotHead() {
        _headCoord = _botCoord.clone();
        if (_direction == Constants.NORTH) {
            _headCoord[1] += 1;
        } else if (_direction == Constants.SOUTH) {
            _headCoord[1] -= 1;
        } else if (_direction == Constants.EAST) {
            _headCoord[0] += 1;
        } else {
            _headCoord[0] -= 1;
        }
    }

    // To be called after every update to maze data
    private void renderMaze() {
        // for testing
        // handleExplore("ff007e00fc01f803f007e00fe01fc03f807f00f8004000000000000000000000000000000003");
        // handleObstacle("041041041060c1e3f3");

        // unexplored
        for (int i = 0; i < MAZE_HEIGHT * MAZE_WIDTH; i++) {
            if (_exploreData[i] == Constants.UNEXPLORED) {
                _tileList.get(i).setState(Constants.UNEXPLORED);
            } else {
                _tileList.get(i).setState(Constants.EXPLORED);
            }
        }

        // set start & end tiles
        if (_coordCount >= 0) {
            ArrayList<MazeTile> targetTiles = getTargetTiles(_startCoord[0], _startCoord[1], 0);
            setTile(targetTiles, Constants.START);
        }
        if (_coordCount == 1) {
            ArrayList<MazeTile> targetTiles = getTargetTiles(_endCoord[0], _endCoord[1], 0);
            setTile(targetTiles, Constants.GOAL);
        }

        // set waypoint tiles
        if (_wpSet) {
            ArrayList<MazeTile> targetTiles = getTargetTiles(_wpCoord[0], _wpCoord[1], 0);
            setTile(targetTiles, Constants.WAYPOINT);
        }

        // set new robot tiles & head
        if (_coordCount >= 0) {
            ArrayList<MazeTile> botTiles = getTargetTiles(_botCoord[0], _botCoord[1], 0);
            setTile(botTiles, Constants.ROBOT_BODY);
            updateBotHead();
            ArrayList<MazeTile> headTile = getTargetTiles(_headCoord[0], _headCoord[1], 3);
            setTile(headTile, Constants.ROBOT_HEAD);
            // for manual mode, update _exploredData array after moving
            if (_inputState == Constants.manualMode) {
                for (MazeTile a : botTiles) {
                    _exploreData[a.get_xPos() + a.get_yPos() * MAZE_WIDTH] = Constants.EXPLORED;
                }
            }
        }

        // obstacles
        for (int i = 0; i < MAZE_HEIGHT * MAZE_WIDTH; i++) {
            if (_obstacleData[i] == 1) {
                _tileList.get(i).setState(Constants.OBSTACLE);
            }
        }

        // arrow blocks
        if (_arrowBlockList.size() > 0) {
            for (Integer[] a : _arrowBlockList) {
                ArrayList<MazeTile> targetTiles = getTargetTiles(a[0], a[1], 3);
                setTile(targetTiles, a[2]); // arrow direction
            }
        }
    }

    /* ====== helper functions ========= */
    // updates tile state(s)
    private void setTile(ArrayList<MazeTile> targetTiles, int newState) {
        if (targetTiles.size() == 0) return;
        for (MazeTile a : targetTiles) setTile(a, newState);
    }

    private void setTile(MazeTile a, int newState) {
        a.setState(newState);
    }

    /**
     * from a selected tile, get the tiles surrounding it, depending on the mode specified
     * mode: 0 -> Block of 9 tiles
     * mode: 1 -> 3 horizontal tiles
     * mode: 2 -> 3 vertical tiles
     * mode: 3 -> single block
     */
    private ArrayList<MazeTile> getTargetTiles(int centerX, int centerY, int mode) {
        // get surrounding tiles
        int _center = centerX + centerY * MAZE_WIDTH;
        ArrayList<MazeTile> _tempList = new ArrayList<MazeTile>();
        try {
            if (mode == 0) {
                _tempList.add(_tileList.get(_center));
                _tempList.add(_tileList.get(_center + 1));
                _tempList.add(_tileList.get(_center - 1));
                _center -= MAZE_WIDTH;
                _tempList.add(_tileList.get(_center));
                _tempList.add(_tileList.get(_center + 1));
                _tempList.add(_tileList.get(_center - 1));
                _center += MAZE_WIDTH * 2;
                _tempList.add(_tileList.get(_center));
                _tempList.add(_tileList.get(_center + 1));
                _tempList.add(_tileList.get(_center - 1));
            } else if (mode == 1) {
                _tempList.add(_tileList.get(_center));
                _tempList.add(_tileList.get(_center + 1));
                _tempList.add(_tileList.get(_center - 1));
            } else if (mode == 2) {
                _tempList.add(_tileList.get(_center));
                _tempList.add(_tileList.get(_center + MAZE_WIDTH));
                _tempList.add(_tileList.get(_center - MAZE_WIDTH));
            } else if (mode == 3) {
                _tempList.add(_tileList.get(_center));
            }
        } catch (Exception e) {
            Log.e("MissingTileIndex", e.getMessage());
        }
        return _tempList;
    }

    // Correct the selected tile, say if it is at the maze edge, shift it inwards
    private int[] correctSelectedTile(int centerX, int centerY, int mode) {
        if (mode == 0 || mode == 1) {
            if (centerX == 0) centerX += 1;
            if (centerX == MAZE_WIDTH - 1) centerX -= 1;
        }
        if (mode == 0 || mode == 2) {
            if (centerY == 0) centerY += 1;
            if (centerY == MAZE_HEIGHT - 1) centerY -= 1;
        }
        int[] result = {centerX, centerY};
        return result;
    }

    private Boolean isObstacle(MazeTile mazeTile) {
        return mazeTile != null && mazeTile.getState() >= Constants.OBSTACLE && mazeTile.getState() <= Constants.WEST;
    }


    // just for converting directions from algo, "N/S/E/W" to our constants
    private int convertDirStrToNum(String dir) {
        int dirNum = 0;
        switch (dir) {
            case "SOUTH":
                dirNum = Constants.SOUTH;
                break;
            case "EAST":
                dirNum = Constants.EAST;
                break;
            case "WEST":
                dirNum = Constants.WEST;
                break;
            case "NORTH":
                dirNum = Constants.NORTH;
                break;
            default:
                dirNum = Constants.NORTH;
                Log.e("robotDir", "Unknown direction: " + dir);
                break;
        }
        return dirNum;
    }

    /**
     * Required Android function for positioning child views, don't change this
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (TILESIZE == 0) {
            int width = this.getWidth();
            int height = this.getHeight();
            TILESIZE = Math.min(width / MAZE_WIDTH, height / MAZE_HEIGHT);
        }

        int i;
        int count = _tileList.size();
        for (i = 0; i < count; i++) {
            int xPos = i % MAZE_WIDTH * TILESIZE;
            int yPos = (MAZE_HEIGHT - 1 - i / MAZE_WIDTH) * TILESIZE;
            _tileList.get(i).layout(xPos, yPos, xPos + TILESIZE, yPos + TILESIZE);
            _tileList.get(i).setPadding(Constants.tilePadding, Constants.tilePadding, Constants.tilePadding, Constants.tilePadding);
        }
    }
}