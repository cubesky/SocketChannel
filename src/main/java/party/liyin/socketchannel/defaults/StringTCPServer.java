package party.liyin.socketchannel.defaults;

import party.liyin.socketchannel.TCPServer;
import party.liyin.socketchannel.callback.TCPSocket;

import java.io.IOException;
import java.net.InetSocketAddress;

public class StringTCPServer extends TCPServer {

    public StringTCPServer(InetSocketAddress address, TCPSocket.SCTCPCallback scTCPCallback) throws IOException {
        super(address, scTCPCallback);
    }

    public StringTCPServer(InetSocketAddress address, TCPSocket.SCTCPCallback scTCPCallback, boolean fullyManagement) throws IOException {
        super(address, scTCPCallback, fullyManagement);
    }

    public void sendBoardcast(String obj) throws IOException {
        super.sendBroadcast(obj.getBytes());
    }

    public void sendMessage(long id, String obj) throws IOException {
        super.sendMessage(id, obj.getBytes());
    }

}