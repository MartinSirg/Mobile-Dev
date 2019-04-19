//
//  ViewController.swift
//  Puzzle15iOS
//
//  Created by Matu VirtualMac on 19/04/2019.
//  Copyright © 2019 Matu VirtualMac. All rights reserved.
//

import UIKit

class ViewController: UIViewController {
    
    @IBOutlet weak var randomizeButton: UIButton!
    @IBOutlet var gameButtons: [UIButton]!
    @IBOutlet weak var timerLabel: UILabel!
    @IBOutlet weak var movesLabel: UILabel!
    
    
    var game : GameBrain = GameBrain()
    /*
    var timer = Timer.scheduledTimer(timeInterval: 1.0, target: self, selector: #selector(onTimerFires), userInfo: nil, repeats: true);
    
    @objc func onTimerFires()
    {
        if (game.isStarted && !game.isSolved){
            game.currentTimer += 1;
            updateStatsUI()
        }
    }
 */
    
    
    @IBAction func buttonClicked(_ sender: UIButton) {
        //TODO: Messages
        let coords = getCoords(tag: sender.tag)
        print("Clicked: ", coords )
        //TODO: toast like thing for error messages?
        game.tileClicked(rowClicked: coords.row, colClicked: coords.col)
        updateUI()
    }
    
    @IBAction func randomizeBoardClicked(_ sender: UIButton) {
        print("reset game");
        game.randomize()
        updateUI()
    }
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        randomizeButton.layer.cornerRadius = 5
        randomizeButton.layer.masksToBounds = true
        
        for btn in gameButtons {
            btn.layer.cornerRadius = 5
            btn.layer.masksToBounds = true
        }
        
        updateUI()
        
    }
    
    func updateUI() {
        for gameButton in gameButtons {
            let coords = getCoords(tag: gameButton.tag)
            let tileValue = game.gameBoard[coords.row][coords.col]
            if (tileValue == 16) {
                gameButton.setTitle("", for: UIControl.State.normal)
            } else{
                gameButton.setTitle("\(tileValue)", for: UIControl.State.normal)
            }
        }
        updateStatsUI()
    }
    
    func updateStatsUI(){
        movesLabel.text = "Moves: \(game.currentMoves)"
        timerLabel.text = "Timer: \(game.currentTimer / 60):\(game.currentTimer % 60)"
    }
    
    func getCoords(tag: Int) -> (row: Int, col:Int) {
        return (tag / 4, tag % 4)
    }


}

