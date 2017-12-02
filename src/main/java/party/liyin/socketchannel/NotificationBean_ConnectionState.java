package party.liyin.socketchannel;

class NotificationBean_ConnectionState extends NotificationBean {
    private TCPSocket.ConnectState connectState;

    NotificationBean_ConnectionState(long id, TCPSocket.ConnectState connectState) {
        this.id = id;
        this.connectState = connectState;
    }

    TCPSocket.ConnectState getConnectState() {
        return connectState;
    }

}