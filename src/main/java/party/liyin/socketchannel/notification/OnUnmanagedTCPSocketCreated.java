package party.liyin.socketchannel.notification;


import java.net.Socket;

public class OnUnmanagedTCPSocketCreated extends BaseNotification {
    private Socket socket;

    public OnUnmanagedTCPSocketCreated(long id, Socket socket) {
        this.id = id;
        this.socket = socket;
    }

    Socket getSocket() {
        return socket;
    }
}
