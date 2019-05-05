//
//  GameBrain.swift
//  Puzzle15iOS
//
//  Created by Matu VirtualMac on 19/04/2019.
//  Copyright © 2019 Matu VirtualMac. All rights reserved.
//

import Foundation



class GameBrain {
    enum Result {
        case emptyTileNotFound, clickedOnEmptyTile, clickedOffBothAxes, gameAlreadySolved, gameSolved, tilesMoved, gameNotStarted
    }
    
    //gameBoard[ROW][COL]
    var gameBoard: [[Int]]
    var currentTimer = 0
    var currentMoves = 0
    var isStarted = false
    var isSolved = false
    var emptyPlaceNumber : Int;
    
    var width: Int;
    var height: Int;
    
    init(width: Int, height: Int) {
        var counter = 1;
        let rows = height < 3 ? 4 : height
        let cols = width < 3 ? 4 : width
        gameBoard = Array(repeating: Array(repeating: -1, count: cols), count: rows)
        for i in 0...(rows - 1){
            for j in 0...(cols - 1){
                gameBoard[i][j] = counter
                counter += 1
            }
        }
        
        self.emptyPlaceNumber = counter - 1
        self.width = cols
        self.height = rows
    }
    
    func randomize() {
        isSolved = false
        isStarted = true
        
        var emptyTileCol = width - 1, emptyTileRow = height - 1;
        var counter = 1;
        for i in 0...(height - 1){
            for j in 0...(width - 1){
                gameBoard[i][j] = counter
                counter += 1
            }
        }
        
        for i in 0...100 {
            if (i % 2 == 0){
                var col : Int
                repeat {
                    col = Int.random(in: 0...(width - 1))
                } while col == emptyTileCol
                tileClicked(rowClicked: emptyTileRow, colClicked: col);
                emptyTileCol = col;
            } else {
                var row: Int;
                repeat{
                    row = Int.random(in: 0...(height - 1))
                } while row == emptyTileRow
                
                tileClicked(rowClicked: row, colClicked: emptyTileCol);
                emptyTileRow = row;

            }
        }
        currentTimer = 0
        currentMoves = 0
    }
    
    func tileClicked(rowClicked: Int, colClicked: Int) -> Result {
        if (isSolved) {return Result.gameAlreadySolved}
        if (!isStarted) {return Result.gameNotStarted}
        
        var rowEmpty = -1, colEmpty = -1
        for i in 0...(height - 1){
            for j in 0...(width - 1){
                if (gameBoard[i][j] == emptyPlaceNumber){
                    rowEmpty = i
                    colEmpty = j
                }
            }
        }
        
        if (colEmpty == -1) { return Result.emptyTileNotFound }
        if (colClicked == colEmpty && rowEmpty == rowClicked) {return Result.clickedOnEmptyTile}
        if (colClicked != colEmpty && rowClicked != rowEmpty) {return Result.clickedOffBothAxes}

        
        if (colClicked == colEmpty){ //Vertical axis
            if (rowClicked > rowEmpty){ //empty tile is above clicked tile
                for i in rowEmpty...rowClicked {
                    if (i == rowClicked) { gameBoard[i][colClicked] = emptyPlaceNumber}
                    else { gameBoard[i][colClicked] = gameBoard[i + 1][colClicked]}
                }
            } else { //empty tile is below clicked tile
                for i in (rowClicked...rowEmpty).reversed() {
                    if (i == rowClicked) { gameBoard[i][colClicked] = emptyPlaceNumber }
                    else { gameBoard[i][colClicked] = gameBoard[i - 1][colClicked] }
                }
            }
        } else { //Horizontal axis
            if (colClicked > colEmpty){//empty tile is to the left of the clicked tile
                for i in colEmpty...colClicked {
                    if (i == colClicked) { gameBoard[rowClicked][i] = emptyPlaceNumber }
                    else { gameBoard[rowClicked][i] = gameBoard[rowClicked][i + 1] }
                }
            }
            else {//empty tile is to the right of clicked the tile
                for i in (colClicked...colEmpty).reversed() {
                    if (i == colClicked) { gameBoard[rowClicked][i] = emptyPlaceNumber }
                    else { gameBoard[rowClicked][i] = gameBoard[rowClicked][i - 1] }
                }
            }
        }
        currentMoves += 1
        
        if (puzzleSolved()){
            isStarted = false;
            isSolved = true;
            return Result.gameSolved;
        }
        return Result.tilesMoved;

        
    }
    
    private func puzzleSolved() -> Bool {
        var counter = 1;
        for i in 0...(height - 1){
            for j in 0...(width - 1){
                if (gameBoard[i][j] == counter) {
                    if (counter == emptyPlaceNumber) {
                        return true
                    }
                    counter += 1
                }
                else {
                    return false
                }

            }
        }
        return false;
    }
    
}
