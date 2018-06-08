package party.liyin.socketchannel;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

    public static class EchoSCTCPCallback extends DefaultSCTCPCallback {
        TCPServer tcpServer;

        public EchoSCTCPCallback(TCPServer tcpServer) {
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

    public static class EchoSCUDPServer extends DefaultSCUDPCallback {
        UDPSocket udpSocket;

        public EchoSCUDPServer(UDPSocket udpSocket) {
            this.udpSocket = udpSocket;
        }

        @Override
        public void onDataArrived(String ip, int port, byte[] obj) {
            try {
                udpSocket.sendMessage(ip, port, obj);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public abstract static class StringSCTCPCallback extends DefaultSCTCPCallback {
        @Override
        public void onDataArrived(long id, byte[] obj) {
            try {
                onDataArrived(id, new String(obj, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        public abstract void onDataArrived(long id, String obj);
    }

    public abstract static class StringSCUDPCallback extends DefaultSCUDPCallback {
        @Override
        public void onDataArrived(String ip, int port, byte[] obj) {
            try {
                onDataArrived(ip, port, new String(obj, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        public abstract void onDataArrived(String ip, int port, String obj);
    }
}
