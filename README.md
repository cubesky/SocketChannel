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
    void onUnmangedCreated(long id, Socket socket){
        //Calling when an unmanaged socket is created.
        //When this is a Server Callback, id is a connection identifier.
        //When this is a Client Callback, id always 0.
        //socket is your raw Socket instance
    }
}
```

#### Server

Now, you can create a `TCPServer` in two lines.
```java
TCPServer tcpServer = new TCPServer(new InetSocketAddress("0.0.0.0", 20000), scTCPCallback); //20000 is your TCPServer Port
tcpServer.start();
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

If you want to create a raw Socket, you can use `createUnmanagedSocket(long)`
```java
tcpServer.createUnmanagedSocket(15L); //Tell id 15 client to connect a new Socket
```

And you can stop is by calling
```java
tcpServer.stop();
```

or
```java
tcpServer.close();
```

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

Send data to your server
```java
tcpClient.sendMessage("Hello Server".getBytes());
```

If you want to create a raw Socket, you can use `createUnmanagedSocket()`
```java
tcpClient.createUnmanagedSocket(); //Tell server to create a new raw Socket
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
UDPSocket udpSocket = new UDPSocket(20000, scUDPCallback); //UDP Server will listen on port 20000
udpSocket.start();
```

Send your data by calling `sendMessage(string, int, byte[])`
```java
udpSocket.sendMessage("127.0.0.1", 20000, "Hello Peer".getBytes());
```

Close it
```java
udpSocket.stop();
```

or

```java
udpSocket.close();
```

You can also use try...resource statement
```java
try (UDPSocket udpSocket = new UDPSocket(20000, scUDPCallback)) {
    udpSocket.start();
} //UDPSocket will automatic close
```

## License

This library is under GPLv3.