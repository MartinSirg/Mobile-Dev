//
//  JsonApi.swift
//  AppleRadio
//
//  Created by Matu VirtualMac on 13/05/2019.
//  Copyright © 2019 Matu VirtualMac. All rights reserved.
//

import Foundation

class JsonApi {
    // @escaping - passed in delegate will live longer then the actual body of function
    func getCurrentSong(jsonUrl: String,
                        completionHandler: @escaping (_ success: Bool, _ artist: String?, _ title: String?)->() ){
        
        get(request: clientUrlRequest(path: jsonUrl)) { (success: Bool, responseObject: Any) in
            DispatchQueue.main.async(execute: {
                if success {
                    var artist = "-"
                    var title = "-"
                    // parse json
                    if let jsonDict = responseObject as? [String: Any],
                        let songHistoryList = jsonDict["SongHistoryList"] as? [Any],
                        let currentSong = songHistoryList.first as? [String: Any]
                    {
                        artist = currentSong["Artist"] as? String ?? "👠"
                        title = currentSong["Title"] as? String ?? "💩"
                    }
                    completionHandler(true, artist, title)
                } else {
                    completionHandler(false, nil, nil)
                }
            })
        }
    }

    
    private func get(request: URLRequest, completionHandler: @escaping (_ success: Bool, _ object: Any) -> ()){
        dataTask(request: request, method: "GET", completionHandler: completionHandler)
    }
    
    private func post(request: URLRequest, completionHandler: @escaping (_ success: Bool, _ object: Any) -> ()){
        dataTask(request: request, method: "POST", completionHandler: completionHandler)
    }
    
    
    
    
    private func dataTask(request: URLRequest,
                          method: String,
                          completionHandler: @escaping (_ success: Bool, _ object: Any?) -> ()){
        var internalRequest = request
        internalRequest.httpMethod = method
        
        let session = URLSession(configuration: URLSessionConfiguration.default)
        session
            .dataTask(with: request, completionHandler: { (data: Data?, response: URLResponse?, error: Error?) in
                if let response = response as? HTTPURLResponse {
                    
                    if response.statusCode < 200 || response.statusCode > 299 {
                        completionHandler(false, nil)
                        return
                    }
                    
                    if let data = data {
                        let json = try? JSONSerialization.jsonObject(with: data, options: [])
                        print(json ?? "json is nil")
                        completionHandler(true, json)
                    }
                }
            })
            .resume()
    }
    
    
    private func clientUrlRequest(path: String) -> URLRequest {
        var request = URLRequest(url: URL(string: path)!)
        
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        return request
    }
}
