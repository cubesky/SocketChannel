package party.liyin.socketchannel.defaults;

import party.liyin.socketchannel.UDPServer;
import party.liyin.socketchannel.callback.UDPSocket;

import java.io.IOException;

public class StringUDPServer extends UDPServer {

    /**
     * Create an UDP Channel
     *
     * @param port          UDP Listen Port
     * @param scUDPCallback UDP Callback
     * @throws IOException
     */
    public StringUDPServer(int port, UDPSocket.SCUDPCallback scUDPCallback) throws IOException {
        super(port, scUDPCallback);
    }

    public void sendMessage(String ip, int port, String obj) throws IOException {
        super.sendMessage(ip, port, obj.getBytes());
    }
}