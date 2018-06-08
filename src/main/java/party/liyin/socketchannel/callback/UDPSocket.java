package party.liyin.socketchannel.callback;

public final class UDPSocket {
    /**
     * UDP Callback
     */
    public interface SCUDPCallback extends SCBaseCallback {
        /**
         * When Data Arrived
         *
         * @param ip   Host
         * @param port Port
         * @param obj  Data Byte Array
         */
        void onDataArrived(String ip, int port, byte[] obj);
    }
}
