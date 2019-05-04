//
//  ViewController.swift
//  Puzzle15iOS
//
//  Created by Matu VirtualMac on 19/04/2019.
//  Copyright © 2019 Matu VirtualMac. All rights reserved.
//

import UIKit

class PuzzleViewController: UIViewController {
    
    @IBOutlet weak var randomizeButton: UIButton!
    @IBOutlet var gameButtons: [UIButton]!
    @IBOutlet weak var timerLabel: UILabel!
    @IBOutlet weak var movesLabel: UILabel!
    
    @IBOutlet weak var tableWrapper: UIStackView!
    
    var inputHeight: Int?;
    var inputWidth: Int?;
    var darkModeEnabled : Bool?;
    
    var gameButtonsNew : [UIButton] = []
    
    var game : GameBrain!
    
    var timer: Timer?
    
    @objc func onTimerFires()
    {
        
        if (game.isStarted && !game.isSolved){
            game.currentTimer += 1;
            updateStatsUI()
        }
    }
    
    
    @IBAction func buttonClicked(_ sender: UIButton) {
        //TODO: Messages
        let coords = getCoords(tag: sender.tag)
        print("Clicked: ", coords )
        //TODO: toast like thing for error messages?
        let result =  game.tileClicked(rowClicked: coords.row, colClicked: coords.col)
        switch result {
            case GameBrain.Result.clickedOnEmptyTile:
                toastMessage("Don't click on empty tile")
        case GameBrain.Result.clickedOffBothAxes:
            toastMessage("Click on a tile on the empty tile's axis")
        case GameBrain.Result.emptyTileNotFound:
            toastMessage("Source code error")
        case GameBrain.Result.gameAlreadySolved:
            toastMessage("Game solved. Press randomize to start again")
        case GameBrain.Result.gameNotStarted:
            toastMessage("Game not started. Press randomize to start")
        case GameBrain.Result.gameSolved:
            toastMessage("Congrats, Puzzle solved!")
        default: break
        }
        updateUI()
    }
    
    @IBOutlet weak var topView: UIView!
    
    @IBAction func randomizeBoardClicked(_ sender: UIButton) {
        print("reset game");
        game.randomize()
        updateUI()
    }
    override func viewDidLoad() {
        super.viewDidLoad()
        
        print("inputWidth = \(inputWidth ?? -1), inputHeight = \(inputHeight ?? -1)")
        if (inputWidth == nil || inputWidth == -1 || inputHeight == nil || inputHeight == -1){
            game = GameBrain(width: 4, height: 4)
        } else{
            game = GameBrain(width: inputWidth!, height: inputHeight!)
        }
        insertButtons(width: game.width,height: game.height);
        
        
        if (darkModeEnabled ?? false){
            view.backgroundColor = #colorLiteral(red: 0.1153683514, green: 0.1153683514, blue: 0.1153683514, alpha: 1)
            randomizeButton.tintColor = #colorLiteral(red: 1, green: 1, blue: 1, alpha: 1)
            timerLabel.textColor = #colorLiteral(red: 1.0, green: 1.0, blue: 1.0, alpha: 1.0)
            movesLabel.textColor = #colorLiteral(red: 1.0, green: 1.0, blue: 1.0, alpha: 1.0)
            
        }
        
        
        // Do any additional setup after loading the view.
        randomizeButton.layer.cornerRadius = 5
        randomizeButton.layer.masksToBounds = true
        randomizeButton.contentEdgeInsets = UIEdgeInsets(top: 5,left: 5,bottom: 5,right: 5)
        
        for btn in gameButtons {
            if (darkModeEnabled ?? false){
                btn.tintColor = #colorLiteral(red: 1, green: 1, blue: 1, alpha: 1)
            }
            btn.layer.cornerRadius = 5
            btn.layer.masksToBounds = true
        }
        timer = Timer.scheduledTimer(timeInterval: 1.0, target: self, selector: #selector(onTimerFires), userInfo: nil, repeats: true)
        updateUI()
        
    }
    
    func insertButtons(width: Int ,height: Int){
        var tagNumber = 0
        /*
        for row in 0...(height - 1) {
            let rowStack = UIStackView()
            rowStack.axis = .horizontal
            rowStack.alignment = .fill
            rowStack.distribution = .fill
            rowStack.spacing = 8.0
            tableWrapper.addArrangedSubview(rowStack)
            
            for col in 0...(width - 1){
                let button = UIButton()
                button.tintColor = self.view.tintColor
                button.backgroundColor = UIColor.darkGray
                button.setTitle("\(tagNumber + 1)", for: .normal)
                button.titleLabel?.font = .systemFont(ofSize: 33)
                button.tag = tagNumber
                button.titleLabel?.textAlignment = NSTextAlignment.center
                button.heightAnchor.constraint(equalTo: button.widthAnchor, multiplier: 1.0/1.0).isActive = true
                rowStack.addArrangedSubview(button)
                
                print("Row: \(row), Col: \(col)")
                tagNumber += 1
            }
        }
 */
    }
    
    func updateUI() {
        UIView.performWithoutAnimation {
            for gameButton in gameButtons {
                let coords = getCoords(tag: gameButton.tag)
                let tileValue = game.gameBoard[coords.row][coords.col]
                if (tileValue == 16) {
                    gameButton.setTitle("", for: UIControl.State.normal)
                } else{
                    gameButton.setTitle("\(tileValue)", for: UIControl.State.normal)
                }
                gameButton.layoutIfNeeded()
            }
            updateStatsUI()
        }
    }
    
    func updateStatsUI(){
        movesLabel.text = "Moves: \(game.currentMoves)"
        timerLabel.text = String(format: "Timer: %02d:%02d", game.currentTimer / 60, game.currentTimer % 60)
    }
    
    func getCoords(tag: Int) -> (row: Int, col:Int) {
        return (tag / game.width, tag % game.width)
    }


}

// https://stackoverflow.com/questions/31540375/how-to-toast-message-in-swift
extension UIViewController {
    func toastMessage(_ message: String){
        guard let window = UIApplication.shared.keyWindow else {return}
        let messageLbl = UILabel()
        messageLbl.text = message
        messageLbl.textAlignment = .center
        messageLbl.font = UIFont.systemFont(ofSize: 12)
        messageLbl.textColor = .white
        messageLbl.backgroundColor = UIColor(white: 0, alpha: 0.5)
        
        let textSize:CGSize = messageLbl.intrinsicContentSize
        let labelWidth = min(textSize.width, window.frame.width - 40)
        
        messageLbl.frame = CGRect(x: 20, y: window.frame.height - 90, width: labelWidth + 30, height: textSize.height + 20)
        messageLbl.center.x = window.center.x
        messageLbl.layer.cornerRadius = messageLbl.frame.height/2
        messageLbl.layer.masksToBounds = true
        window.addSubview(messageLbl)
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
            
            UIView.animate(withDuration: 2, animations: {
                messageLbl.alpha = 0
            }) { (_) in
                messageLbl.removeFromSuperview()
            }
        }
    }}
