package party.liyin.socketchannel;

import java.net.Socket;

class NotificationBean_UnmanagedCreated extends NotificationBean {
    private Socket socket;

    NotificationBean_UnmanagedCreated(long id, Socket socket) {
        this.id = id;
        this.socket = socket;
    }

    Socket getSocket() {
        return socket;
    }
}
