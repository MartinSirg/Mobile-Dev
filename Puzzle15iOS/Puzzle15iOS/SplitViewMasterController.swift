//
//  MsterSplitViewController.swift
//  Puzzle15iOS
//
//  Created by Matu VirtualMac on 04/05/2019.
//  Copyright © 2019 Matu VirtualMac. All rights reserved.
//

import UIKit

class SplitViewMasterController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        
        widthStepper.maximumValue = 10
        heightStepper.maximumValue = 10
        
        widthStepper.value = 4
        heightStepper.value = 4
        
        widthStepper.minimumValue = 3
        heightStepper.minimumValue = 3
        // Do any additional setup after loading the view.
    }
    
    @IBOutlet weak var widthValueLabel: UILabel!
    @IBOutlet weak var heightValueLabel: UILabel!
    
    @IBOutlet weak var heightStepper: UIStepper!
    @IBOutlet weak var widthStepper: UIStepper!
    
    @IBAction func widthStepperValueChange(_ sender: UIStepper) {
        print(sender.value)
        widthValueLabel.text = "\(Int(sender.value))"
    }
    
    @IBAction func darkModeToggle(_ sender: UISwitch) {
        
    }
    @IBOutlet weak var darkModeToggleButton: UISwitch!
    @IBAction func heightStepperValueChange(_ sender: UIStepper) {
        print(sender.value)
        heightValueLabel.text = "\(Int(sender.value))"
    }
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
        
        print(segue.identifier ?? "NIL Segue identifier")
        
        if let segueId = segue.identifier {
            switch segueId {
            case "PuzzleSegue":
                if let vc = segue.destination as? PuzzleViewController{
                    vc.inputWidth = Int(widthValueLabel.text ?? "4")
                    vc.inputHeight = Int(heightValueLabel.text ?? "4")
                    vc.darkModeEnabled = darkModeToggleButton.isOn
                }
                break
            default:
                break
            }
        }
    }
    

}
