//
//  ViewController.swift
//  AppleRadio
//
//  Created by Matu VirtualMac on 13/05/2019.
//  Copyright © 2019 Matu VirtualMac. All rights reserved.
//

import UIKit

class ViewController: UIViewController, UIPickerViewDelegate, UIPickerViewDataSource {
    
    var tabBar : AppTabBarController!
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return tabBar.stations.count
    }
    
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        return tabBar.stations[row].name
    }
    
    var timer: Timer?
    
    @objc func onTimerFires()
    {
        jsonApi.getCurrentSong(jsonUrl: tabBar.currentStation().songInfoUrl, completionHandler: songInfoReceived)
    }

    

    let jsonApi = JsonApi()
    @IBOutlet weak var artistLabel: UILabel!
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var stationPicker: UIPickerView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        tabBar = tabBarController as? AppTabBarController
        // Do any additional setup after loading the view.
        stationPicker.delegate = self
        stationPicker.dataSource = self
    }

    @IBAction func mainButtonClicked(_ sender: UIButton) {
        timer = Timer.scheduledTimer(timeInterval: 15.0, target: self, selector: #selector(onTimerFires), userInfo: nil, repeats: true)
        jsonApi.getCurrentSong(jsonUrl: tabBar.currentStation().songInfoUrl, completionHandler: songInfoReceived)
    }
    
    
    
    
    private func songInfoReceived(success: Bool, artist: String?, title: String?){
        if (!success) {
            artistLabel.text = "Error while loading artist"
            titleLabel.text = "Error while loading title"
            return
        }
        if (artist == nil || title == nil) {return}
        
        artistLabel.text = artist
        titleLabel.text = title
        
        if tabBar.currentStation().lastSong == title && tabBar.currentStation().lastArtist == artist {
            return
        }
        print("Adding data")
        
        let artistExists = tabBar.currentStation().artists.contains { (element: Artist) in return element.name == artist }
        var artistObject: Artist
        
        if !artistExists {
            artistObject = Artist(name: artist!)
            tabBar.currentStation().artists.append(artistObject)
        } else {
            artistObject = tabBar.currentStation().artists
                .first { (element: Artist) in return element.name == artist }!
        }
        
        artistObject.uniqueSongs[title!] = (artistObject.uniqueSongs[title!] ?? 0)  + 1
        
        tabBar.currentStation().lastArtist = artist!
        tabBar.currentStation().lastSong = title!
        
    }
    
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        tabBar.currentStationIndex = row
        jsonApi.getCurrentSong(jsonUrl: tabBar.currentStation().songInfoUrl, completionHandler: songInfoReceived)
    }
}

