<br/>
<p align="center">
    <a href="https://github.com/TheArchitect123/TitanSocket"><img src="./kotlin.jpg" align="center" width=350/></a>
</p>

<p align="center">
A kotlin multiplatform library to manage sockets with support for both iOS & Android

</p>
<br/>
![version](https://img.shields.io/badge/version-0.0.2-blue)
![targets](https://img.shields.io/badge/targets-JVM,_Android,_iOS-white.svg)

## How it works
TitanSocket handles all the websocket connections, ping & pong between the client & server, and the event notifications for when data is received or broadcasted, or any connectivity status changes happen.

To get started, import the library into your project:

```sh
implementation("io.github.thearchitect123:titansocket:0.0.1")
```

To use TitanSocket, generate an instance of your socket, pass the Url + any Post Connection Logic, and subscribe to the states you wish to connect to:

```sh
val socketConnection = TitanSocket("wss://mysupersecret/websocket"){
      subscribeOn(TitanSocketEvent.Connection) {
      }
      subscribeOn(TitanSocketEvent.Disconnection) {
      }
}
```

Make sure to close your web socket connection after you are done with it to avoid battery issues.

```sh
socketConnection.disconnectSocket()
```

## License

This software is licensed under the MIT license. See [LICENSE](./LICENSE) for full disclosure.
