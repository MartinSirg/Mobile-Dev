//
//  Artist.swift
//  AppleRadio
//
//  Created by Matu VirtualMac on 13/05/2019.
//  Copyright © 2019 Matu VirtualMac. All rights reserved.
//

import Foundation

class Artist {
    var id: Int
    var name: String
    var stationId: Int
    var uniqueSongs: [String : Int] = [:]
    
    init(name: String) {
        self.id = 0
        self.name = name
        self.stationId = 0
    }
}
