package party.liyin.socketchannel;

class NotificationTask implements Runnable {
    private SCBaseCallback callback;
    private Notification bean;

    NotificationTask(SCBaseCallback callback, Notification bean) {
        this.callback = callback;
        this.bean = bean;
    }

    @Override
    public void run() {
        if (callback instanceof TCPSocket.SCTCPCallback) {
            if (bean instanceof ConnectionStateNotification) {
                ((TCPSocket.SCTCPCallback) callback).onManagedConnectState(bean.getId(), ((ConnectionStateNotification) bean).getConnectState());
            } else if (bean instanceof TCPDataArrivedNotification) {
                ((TCPSocket.SCTCPCallback) callback).onDataArrived(bean.getId(), ((TCPDataArrivedNotification) bean).getObj());
            } else if (bean instanceof UnmanagedTCPSocketCreatedNotification) {
                ((TCPSocket.SCTCPCallback) callback).onUnmangedCreated(bean.getId(), ((UnmanagedTCPSocketCreatedNotification) bean).getSocket());
            } else {
                System.err.println("Notification NotSupport TCP");
            }
        } else if (callback instanceof UDPSocket.SCUDPCallback) {
            if (bean instanceof UDPDataArrivedNotification) {
                ((UDPSocket.SCUDPCallback) callback).onDataArrived(((UDPDataArrivedNotification) bean).getHost(), ((UDPDataArrivedNotification) bean).getPort(), ((UDPDataArrivedNotification) bean).getObj());
            } else {
                System.err.println("Notification NotSupport UDP");
            }
        } else {
            System.err.println("Not Support");
        }
    }
}