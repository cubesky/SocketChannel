package party.liyin.socketchannel.defaults;

import party.liyin.socketchannel.TCPClient;
import party.liyin.socketchannel.callback.TCPSocket;

import java.net.InetSocketAddress;

public class StringTCPClient extends TCPClient {

    public StringTCPClient(InetSocketAddress address, TCPSocket.SCTCPCallback scTCPCallback) {
        super(address, scTCPCallback);
    }

    public StringTCPClient(InetSocketAddress address, TCPSocket.SCTCPCallback scTCPCallback, boolean fullyManagement) {
        super(address, scTCPCallback, fullyManagement);
    }

    public void sendMessage(String obj) {
        super.sendMessage(obj.getBytes());
    }
}
