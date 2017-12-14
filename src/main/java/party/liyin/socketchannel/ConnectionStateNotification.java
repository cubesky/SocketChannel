package party.liyin.socketchannel;

class ConnectionStateNotification extends Notification {
    private TCPSocket.ConnectState connectState;

    ConnectionStateNotification(long id, TCPSocket.ConnectState connectState) {
        this.id = id;
        this.connectState = connectState;
    }

    TCPSocket.ConnectState getConnectState() {
        return connectState;
    }

}