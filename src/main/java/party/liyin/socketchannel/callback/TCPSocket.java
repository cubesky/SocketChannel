package party.liyin.socketchannel.callback;

import java.net.Socket;

public final class TCPSocket {
    /**
     * TCP Connection State
     */
    public enum ConnectState {
        CONNECT, DISCONNECT, CLOSED
    }

    public interface SCTCPConnectResultCallback {
        void onConnect();

        void onError();
    }

    /**
     * TCP Callback
     */
    public interface SCTCPCallback extends SCBaseCallback {
        /**
         * When Data Arrived
         *
         * @param id  ID of Client, if this is a client, id always 0
         * @param obj Data Byte Array
         */
        void onDataArrived(long id, byte[] obj);

        /**
         * Connection State Changed
         *
         * @param id           ID of Client
         * @param connectState Connection State
         */
        void onManagedConnectState(long id, ConnectState connectState);

        /**
         * An Unmanaged Socket created on Client ID.
         *
         * @param id     Client ID
         * @param socket Connected Raw Socket
         */
        void onUnmangedCreated(long id, String tag, Socket socket);

        /**
         * When Heartbeat Arrived
         *
         * @param id ID of Client, if this is a client, id always 0
         */
        void onHeartbeat(long id);
    }

    /**
     * TCP Auth Callback
     */
    public interface SCAuthCallback extends SCBaseCallback {
        /**
         * Auth Method
         * @param socket Socket with remote information
         * @return If the connection should be accept
         */
        boolean onNewSocketAuth(Socket socket);
    }
}















