package party.liyin.socketchannel.notification;

import party.liyin.socketchannel.callback.TCPSocket;

public class OnConnectionStateChanged extends BaseNotification {
    private TCPSocket.ConnectState connectState;

    public OnConnectionStateChanged(long id, TCPSocket.ConnectState connectState) {
        this.id = id;
        this.connectState = connectState;
    }

    TCPSocket.ConnectState getConnectState() {
        return connectState;
    }

}