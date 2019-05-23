//
//  AppTabBarController.swift
//  AppleRadio
//
//  Created by Matu VirtualMac on 13/05/2019.
//  Copyright © 2019 Matu VirtualMac. All rights reserved.
//

import UIKit

class AppTabBarController: UITabBarController {
    
    let stations : [Station] = [
        Station(name: "Sky Plus", streamUrl: "-", songInfoUrl: "http://dad.akaver.com/api/SongTitles/SP"),
        Station(name: "NRJ", streamUrl: "-", songInfoUrl: "http://dad.akaver.com/api/SongTitles/NRJ"),
        Station(name: "RockFm", streamUrl: "-", songInfoUrl: "http://dad.akaver.com/api/SongTitles/ROCKFM")
    ]
    
    func currentStation() -> Station {
        return stations[currentStationIndex]
    }
    
    var currentStationIndex = 0;

    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Do any additional setup after loading the view.
    }
    

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */

}
