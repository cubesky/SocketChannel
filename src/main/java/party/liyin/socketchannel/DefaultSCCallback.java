package party.liyin.socketchannel;

import java.net.Socket;

public class DefaultSCCallback {
    public static class DefaultSCTCPCallback implements TCPSocket.SCTCPCallback {
        @Override
        public void onDataArrived(long id, byte[] obj) {

        }

        @Override
        public void onManagedConnectState(long id, TCPSocket.ConnectState connectState) {

        }

        @Override
        public void onUnmangedCreated(long id, Socket socket) {

        }
    }

    public static class DefaultSCUDPCallback implements UDPSocket.SCUDPCallback {
        @Override
        public void onDataArrived(String ip, int port, byte[] obj) {

        }
    }
}
