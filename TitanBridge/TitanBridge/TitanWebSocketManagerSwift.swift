
import Foundation

public typealias passResult = (String) -> Void
@objc public class TitanWebSocketManager : NSObject {
   
    @objc public class func connectToWebSocket(remoteUrl: String, postBody: String, postResult: @escaping passResult){

        let urlSession = URLSession(configuration: .default)
        let webSocketTask = urlSession.webSocketTask(with: URL(string: remoteUrl)!)

        webSocketTask.resume()
        webSocketTask.sendPing { error in
            
        }

        webSocketTask.send(URLSessionWebSocketTask.Message.string(postBody.trimmingCharacters(in: .whitespacesAndNewlines))) { error in
            webSocketTask.receive { result in
                switch(result){
                case .success(let data):
                    postResult("\(data)")
                case .failure(let error):
                    print("ERROR : \(error.localizedDescription)")
                }
            }
        }
    }

    
}
