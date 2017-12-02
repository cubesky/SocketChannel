package party.liyin.socketchannel;

class NotificationRunnable implements Runnable {
    private SkyBaseCallback callback;
    private NotificationBean bean;

    NotificationRunnable(SkyBaseCallback callback, NotificationBean bean) {
        this.callback = callback;
        this.bean = bean;
    }

    @Override
    public void run() {
        if (callback instanceof TCPSocket.SkyTCPCallback) {
            if (bean instanceof NotificationBean_ConnectionState) {
                ((TCPSocket.SkyTCPCallback) callback).onManagedConnectState(bean.getId(), ((NotificationBean_ConnectionState) bean).getConnectState());
            } else if (bean instanceof NotificationBean_DataArrived) {
                ((TCPSocket.SkyTCPCallback) callback).onDataArrived(bean.getId(), ((NotificationBean_DataArrived) bean).getObj());
            } else if (bean instanceof NotificationBean_UnmanagedCreated) {
                ((TCPSocket.SkyTCPCallback) callback).onUnmangedCreated(bean.getId(), ((NotificationBean_UnmanagedCreated) bean).getSocket());
            } else {
                System.err.println("Notification NotSupport TCP");
            }
        } else if (callback instanceof UDPSocket.SkyUDPCallback) {
            if (bean instanceof NotificationBean_DataArrived_UDP) {
                ((UDPSocket.SkyUDPCallback) callback).onDataArrived(((NotificationBean_DataArrived_UDP) bean).getHost(), ((NotificationBean_DataArrived_UDP) bean).getPort(), ((NotificationBean_DataArrived_UDP) bean).getObj());
            } else {
                System.err.println("Notification NotSupport UDP");
            }
        } else {
            System.err.println("Not Support");
        }
    }
}