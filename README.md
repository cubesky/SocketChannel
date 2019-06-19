# A Java NIO Connection Library

Author: CubeSky

An easy used Java NIO Library. Use callback automaticlly notify user to data arrived.

## Install

You can use `Cube Repo` at [https://cubesky-mvn.github.io/](https://cubesky-mvn.github.io/)

## API

### TCP

First, you need a listener for `TCPServer` or `TCPClient`
```java
TCPSocket.SCTCPCallback scTCPCallback = new TCPSocket.SCTCPCallback(){
    void onDataArrived(long id, byte[] obj) {
        //Calling when data arrived.
        //When this is a Server Callback, id is a connection identifier.
        //When this is a Client Callback, id always 0.
        //obj is data byte array.
    }
    void onManagedConnectState(long id, ConnectState connectState) {
        //Calling when a managed connection state changed.
        //When this is a Server Callback, id is a connection identifier.
        //When this is a Client Callback, id always 0.
        //connectState is an Enum.
    }
    void onUnmangedCreated(long id, String tag, Socket socket){
        //Calling when an unmanaged socket is created.
        //When this is a Server Callback, id is a connection identifier.
        //When this is a Client Callback, id always 0.
        //tag is the tag when create this socket
        //socket is your raw Socket instance
    }
    void onHeartbeat(long id) {
        //Calling when heartbeat arrived.
        //When this is a Server Callback, id is a connection identifier.
        //When this is a Client Callback, id always 0.
    }
}
```

#### Server

Now, you can create a `TCPServer` in two lines.
```java
TCPServer tcpServer = new TCPServer(new InetSocketAddress("0.0.0.0", 20000), scTCPCallback); //20000 is your TCPServer Port
tcpServer.start();
```

##### Fully Management Mode
This library will add a byte as command code before your data, if you don't want to use `createUnmanagedSocket` and Auto Heartbeat feature, you can use Manually Mode to disable this feature.
If you do this, `createUnmanagedSocket` will immediately return without do anything. You need create `TCPClient` as Manually Mode too.
```java
TCPServer tcpServer = new TCPServer(new InetSocketAddress("0.0.0.0", 20000), scTCPCallback, true);
```

You can send data by using `sendMessage(long,byte[])`
```java
tcpServer.sendMessage(15L, "Hello Client".getBytes()); //15L is client id
```

You can also send Boardcast to all connected client by using `sendBoardcast(byte[])`
```java
tcpServer.sendBoardcast("Hello All Client".getBytes());
```

If you want to know if two id is the same host, you can simplify call `isSameHost(long, long)`
```java
tcpServer.isSameHost(15L, 20L); //15L and 20L is two client id, return a boolean
```

If you want to create a raw Socket, you can use `createUnmanagedSocket(long)` or `createUnmanagedSocket(long, String)`
```java
tcpServer.createUnmanagedSocket(15L); //Tell id 15 client to connect a new Socket without tag
tcpServer.createUnmanagedSocket(15L, "Tag"); //Tell id 15 client to connect a new Socket with tag
```

And you can stop is by calling
```java
tcpServer.stop();
```

or
```java
tcpServer.close();
```

If you want to filter some connection during new connection accept. You can use `setConnAuthCallback`
```java
tcpServer.setConnAuthCallback(new TCPSocket.SCAuthCallback(){
    boolean onNewSocketAuth(Socket socket) {
      //Your code
      return true; //true to Accept, false to Deny
    }
})
```

> Becareful: This callback is sync, so it will block the main loop, use it carefully or you will get a poor performance.

you can also use try..resource statement
```java
try(TCPServer tcpServer = new TCPServer(new InetSocketAddress("0.0.0.0", 20000), scTCPCallback)){ //20000 is your TCPServer Port
    tcpServer.start();
} //TCPServer will automatic close
```

#### Client

Client is more easy.
```java
TCPClient tcpClient = new TCPClient(new InetSocketAddress("127.0.0.1", 20000), scTCPCallback); //127.0.0.1 is your server ip and 20000 is your server port
tcpClient.start();
```

or

```java
TCPClient tcpClient = new TCPClient(new InetSocketAddress("127.0.0.1", 20000), scTCPCallback); //127.0.0.1 is your server ip and 20000 is your server port
tcpClient.startAsync(new TCPSocket.SCTCPConnectResultCallback() {
    void onConnect() {
        //This will call after global CONNECTED callback, so you can ignore in default.
    }
    
    void onError() {
        //When Connection Error
    }
});
```

##### Fully Management Mode
This library will add a byte as command code before your data, if you don't want to use `createUnmanagedSocket` and Auto Heartbeat feature, you can use this to disable this feature.
If you do this, `createUnmanagedSocket` will immediately return without do anything. You need create `TCPSocket` as Manually Mode too.
```java
TCPClient tcpClient = new TCPClient(new InetSocketAddress("127.0.0.1", 20000), scTCPCallback, true);
```

Send data to your server
```java
tcpClient.sendMessage("Hello Server".getBytes());
```

If you want to create a raw Socket, you can use `createUnmanagedSocket()` or `createUnmanagedSocket(String)`
```java
tcpClient.createUnmanagedSocket(); //Tell server to create a new raw Socket without tag
tcpClient.createUnmanagedSocket("Tag"); //Tell server to create a new raw Socket with tag
```

And you can stop is by calling
```java
tcpClient.stop();
```

or
```java
tcpClient.close();
```

you can also use try..resource statement
```java
try(TCPClient tcpClient = new TCPClient(new InetSocketAddress("127.0.0.1", 20000), scTCPCallback)){ //127.0.0.1 is your server ip and 20000 is your server port
    tcpClient.start();
} //TCPClient will automatic close
```

#### ConnectState Enum
 - `CONNECT`: when a connection is created and ready to use
 - `DISCONNECT`: when a connection is broken.
 - `CLOSED`: TCPServer or TCPClient is closed, it can not used for new connection, connection which is connected will lose.


### UDP

UDP is easy too.
```java
SCUDPCallback scUDPCallback = new SCUDPCallback() {
  void onDataArrived(String ip, int port, byte[] obj) {
    //Calling when data arrived
  }
}
UDPServer udpServer = new UDPServer(20000, scUDPCallback); //UDP Server will listen on port 20000
udpServer.start();
```

Send your data by calling `sendMessage(string, int, byte[])`
```java
udpServer.sendMessage("127.0.0.1", 20000, "Hello Peer".getBytes());
```

Close it
```java
udpServer.stop();
```

or

```java
udpServer.close();
```

You can also use try...resource statement
```java
try (UDPServer udpServer = new UDPServer(20000, scUDPCallback)) {
    udpServer.start();
} //UDPSocket will automatic close
```

## Shortcut
### Server and Client
`StringTCPClient` `StringTCPServer` `StringUDPServer` is comming after `SocketChannel 7.0`.   
They will automatically wrap byte array to String UTF-8.  
`FileTCPClient` `FileTCPServer` is comming after `SocketChannel 8.0`.  
They provide method to send file directly.  
`FileStringTCPClient` `FileStringTCPServer` is comming after `SocketChannel 8.0`.  
They provide method to send file directly and extend from `StringTCPClient` and `StringTCPServer`.   

### Callback
`DefaultTCPCallback` and `DefaultUDPCallback` is an empty `SCTCPCallback` and `SCUDPCallback` implementation. You can extends and override these to decrease your code.  
`EchoTCPCallback` and `EchoUDPCallback` will offer an echo server handler automatically.  
`StringTCPCallback` and `StringUDPCallback` will automatically transform byte array to String UTF-8.   
`FileDefaultTCPCallback` and `FileStringTCPCallback` extends `DefaultTCPCallback` and `StringTCPCallback` with file transport support.  

### TransportHelper
Transport Helper will help to transport non-standard data.  
Now, you can use `FileTransportHelper` to transport files between socket channel instance.  

> All Transport Helper can not run with Manually Mode instance!  

## Heartbeat
If you use fully management mode, SocketChannel library will automatically send and reply heartbeat packet every 2 minutes using command code 2.  
In Manually Mode, this feature will be disabled.

## ChannelWrapper
Use ChannelWrapper to transform unsupport NIO stream to NIO Channel.
### SystemInChannel
This channel transform `System.in` to an `Pipe.SourceChannel`, call `ChannelWrapper.SystemIn.getSystemInChannel()` to get Channel. Be careful, only one Channel will be create, even if you call this method many times!

## Dependency

| Name  | Description                    | Package Name       |  
|:--    |:--                             |:--                 |  
| Guava | Google Core Libraries for Java | `com.google.guava` |  

## License

This library is under GPLv3.