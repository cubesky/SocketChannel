package party.liyin.socketchannel.callback;

import party.liyin.socketchannel.UDPServer;

import java.io.IOException;

public class EchoUDPServer extends DefaultUDPCallback {
    private UDPServer udpServer;

    public EchoUDPServer(UDPServer udpServer) {
        this.udpServer = udpServer;
    }

    @Override
    public void onDataArrived(String ip, int port, byte[] obj) {
        try {
            udpServer.sendMessage(ip, port, obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}