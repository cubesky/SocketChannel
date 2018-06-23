package party.liyin.socketchannel.notification;


import java.net.Socket;

public class OnUnmanagedTCPSocketCreated extends BaseNotification {
    private Socket socket;
    private String tag;

    public OnUnmanagedTCPSocketCreated(long id, String tag, Socket socket) {
        this.id = id;
        this.tag = tag;
        this.socket = socket;
    }

    Socket getSocket() {
        return socket;
    }

    public String getTag() {
        return tag;
    }
}
