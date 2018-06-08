package party.liyin.socketchannel;

import java.io.IOException;
import java.net.InetSocketAddress;

public class DefaultServer {
    public static class DefaultTCPServer extends TCPServer {

        public DefaultTCPServer(InetSocketAddress address, TCPSocket.SCTCPCallback scTCPCallback) throws IOException {
            super(address, scTCPCallback);
        }

        public DefaultTCPServer(InetSocketAddress address, TCPSocket.SCTCPCallback scTCPCallback, boolean fullyManagement) throws IOException {
            super(address, scTCPCallback, fullyManagement);
        }

        public void sendBoardcast(String obj) throws IOException {
            super.sendBroadcast(obj.getBytes());
        }

        public void sendMessage(long id, String obj) throws IOException {
            super.sendMessage(id, obj.getBytes());
        }

    }

    public static class DefaultTCPClient extends TCPClient {

        public DefaultTCPClient(InetSocketAddress address, TCPSocket.SCTCPCallback scTCPCallback) {
            super(address, scTCPCallback);
        }

        public DefaultTCPClient(InetSocketAddress address, TCPSocket.SCTCPCallback scTCPCallback, boolean fullyManagement) {
            super(address, scTCPCallback, fullyManagement);
        }

        public void sendMessage(String obj) {
            super.sendMessage(obj.getBytes());
        }
    }

    public static class DefaultUDPSocket extends UDPSocket {

        /**
         * Create an UDP Channel
         *
         * @param port          UDP Listen Port
         * @param scUDPCallback UDP Callback
         * @throws IOException
         */
        public DefaultUDPSocket(int port, SCUDPCallback scUDPCallback) throws IOException {
            super(port, scUDPCallback);
        }

        public void sendMessage(String ip, int port, String obj) throws IOException {
            super.sendMessage(ip, port, obj.getBytes());
        }
    }
}
