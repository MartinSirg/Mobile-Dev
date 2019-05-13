//
//  Station.swift
//  AppleRadio
//
//  Created by Matu VirtualMac on 13/05/2019.
//  Copyright © 2019 Matu VirtualMac. All rights reserved.
//

import Foundation

class Station {
    var id : Int
    var name : String
    var streamUrl: String
    var songInfoUrl : String
    
    var lastArtist = ""
    var lastSong = ""
    var artists : [Artist] = []
    
    init(name: String, streamUrl: String, songInfoUrl: String) {
        self.id = 0
        self.name = name
        self.songInfoUrl = songInfoUrl
        self.streamUrl = streamUrl
    }
}
