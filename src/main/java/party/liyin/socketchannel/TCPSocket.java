package party.liyin.socketchannel;

import java.net.Socket;

public class TCPSocket {
    /**
     * TCP Connection State
     */
    public enum ConnectState {
        CONNECT, DISCONNECT, CLOSED
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
        void onUnmangedCreated(long id, Socket socket);
    }
}















