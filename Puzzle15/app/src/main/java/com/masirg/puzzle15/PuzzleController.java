package com.masirg.puzzle15;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

class PuzzleController {
    static private final String TAG = PuzzleController.class.getSimpleName();
    private static PuzzleController instance;

    boolean isStarted;
    boolean isSolved = false;
    int currentTimer = 0;
    int currentMoves = 0;
    int bestMoves = 0;
    boolean solutionToggled = false;

    //1 2 3 4
    //5 6 7 8 ...
    //16 represents empty tile
    int[][] tiles;
    int[][] bufferTiles;

    private PuzzleController() {
        tiles = new int[][]{{1,2,3,4},{5,6,7,8},{9,10,11,12},{13,14,15,16}};
        isStarted = false;
    }

    static PuzzleController getInstance() {
        if (instance == null) instance = new PuzzleController();
        return instance;
    }

    int tileClicked(int rowClicked, int colClicked) throws Exception {
        if (isSolved) return C.GAME_ALREADY_SOLVED;
        if (solutionToggled) return C.SOLUTION_TOGGLED;
        if (!isStarted) return C.NOT_STARTED;

        int rowEmpty = -1, colEmpty = -1;
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[i].length; j++) {
                if (tiles[i][j] == 16) {
                    rowEmpty = i;
                    colEmpty = j;
                }
            }
        }
        if (colEmpty == -1) throw new Exception("Didn't find tile valued 16(empty). Don't Modify tile values manually!");
        if (colClicked == colEmpty && rowEmpty == rowClicked) return C.CLICKED_ON_EMPTY_TILE;
        if (colClicked != colEmpty && rowClicked != rowEmpty) return C.CLICKED_OFF_BOTH_VALID_AXES;

        // Moving tiles around

        if (colClicked == colEmpty){ //Vertical axis
            if (rowClicked > rowEmpty){ //empty tile is above clicked tile
                for (int i = rowEmpty; i <= rowClicked; i++) {
                    if (i == rowClicked) tiles[i][colClicked] = 16;
                    else  tiles[i][colClicked] = tiles[i + 1][colClicked];
                }
            } else { //empty tile is below clicked tile
                for (int i = rowEmpty; i >= rowClicked; i--) {
                    if (i == rowClicked) tiles[i][colClicked] = 16;
                    else  tiles[i][colClicked] = tiles[i - 1][colClicked];
                }
            }
        } else { //Horizontal axis
            if (colClicked > colEmpty){//empty tile is to the left of the clicked tile
                for (int i = colEmpty; i <= colClicked; i++){
                    if (i == colClicked) tiles[rowClicked][i] = 16;
                    else tiles[rowClicked][i] = tiles[rowClicked][i + 1];
                }
            }
            else {//empty tile is to the right of clicked the tile
                for (int i = colEmpty; i >= colClicked; i--){
                    if (i == colClicked) tiles[rowClicked][i] = 16;
                    else tiles[rowClicked][i] = tiles[rowClicked][i - 1];
                }
            }
        }
        currentMoves++;
        if (checkIsFinished()){
            isStarted = false;
            isSolved = true;
            if (bestMoves > currentMoves || bestMoves == 0) bestMoves = currentMoves;
            return C.GAME_SOLVED;
        }
        return C.TILES_MOVED;

    }

    // If game is in solved state returns true, sets started false and solved true;
    private boolean checkIsFinished() {
        int counter = 1;
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[i].length; j++) {
                if (tiles[i][j] == counter) {
                    if (counter == 16) {
                        return true;
                    }
                    counter++;
                }
                else return false;
            }
        }
        return false;
    }

    void randomize() {
        if (isSolved) isSolved = false;
        if (!isStarted) isStarted = true;
        tiles = new int[][]{{1,2,3,4},{5,6,7,8},{9,10,11,12},{13,14,15,16}};
        int emptyTileCol = 3, emptyTileRow = 3;
        try {
            do {
                for (int i = 0; i < 100; i++) {
                    if (i % 2 == 0){ //Picks horizontal axis
                        int col;
                        do col = ThreadLocalRandom.current().nextInt(0, 4);
                        while (col == emptyTileCol);

                        tileClicked(emptyTileRow, col);
                        emptyTileCol = col;
                    } else { //Picks vertical axis
                        int row;
                        do row = ThreadLocalRandom.current().nextInt(0, 4);
                        while (row == emptyTileRow);

                        tileClicked(row, emptyTileCol);
                        emptyTileRow = row;
                    }
                }
            //Randomizes again, if randomized array happens to be the solution
            } while (Arrays.equals(tiles, new int[][]{{1,2,3,4},{5,6,7,8},{9,10,11,12},{13,14,15,16}}));


        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        currentMoves = 0;
        currentTimer = 0;
    }

    void toggleSolution(){
        if (solutionToggled){
            for (int i = 0; i < bufferTiles.length; i++) {
                tiles[i] = new int[4];
                if (bufferTiles[i].length >= 0)
                    System.arraycopy(bufferTiles[i], 0, tiles[i], 0, bufferTiles[i].length);
            }
            bufferTiles = null;
            solutionToggled = false;
            return;
        }
        solutionToggled = true;
        bufferTiles = new int[4][4];
        for (int i = 0; i < tiles.length; i++) {
            bufferTiles[i] = new int[4];
            if (tiles[i].length >= 0)
                System.arraycopy(tiles[i], 0, bufferTiles[i], 0, tiles[i].length);
        }

        tiles = new int[][]{{1,2,3,4},{5,6,7,8},{9,10,11,12},{13,14,15,16}};
    }
}
