package party.liyin.socketchannel.callback;

import party.liyin.socketchannel.TCPServer;

import java.io.IOException;

public class EchoTCPCallback extends DefaultTCPCallback {
    private TCPServer tcpServer;

    public EchoTCPCallback(TCPServer tcpServer) {
        this.tcpServer = tcpServer;
    }

    @Override
    public void onDataArrived(long id, byte[] obj) {
        try {
            tcpServer.sendMessage(id, obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
