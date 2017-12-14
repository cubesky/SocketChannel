package party.liyin.socketchannel;

import java.net.Socket;

class UnmanagedTCPSocketCreatedNotification extends Notification {
    private Socket socket;

    UnmanagedTCPSocketCreatedNotification(long id, Socket socket) {
        this.id = id;
        this.socket = socket;
    }

    Socket getSocket() {
        return socket;
    }
}
