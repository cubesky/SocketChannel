package party.liyin.socketchannel.callback;

import java.net.Socket;

public class DefaultTCPCallback implements TCPSocket.SCTCPCallback {
    @Override
    public void onDataArrived(long id, byte[] obj) {

    }

    @Override
    public void onManagedConnectState(long id, TCPSocket.ConnectState connectState) {

    }

    @Override
    public void onUnmangedCreated(long id, Socket socket) {

    }

    @Override
    public void onHeartbeat(long id) {

    }
}