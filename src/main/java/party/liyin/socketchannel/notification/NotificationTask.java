package party.liyin.socketchannel.notification;

import party.liyin.socketchannel.callback.SCBaseCallback;
import party.liyin.socketchannel.callback.TCPSocket;
import party.liyin.socketchannel.callback.UDPSocket;

public class NotificationTask implements Runnable {
    private SCBaseCallback callback;
    private BaseNotification bean;

    public NotificationTask(SCBaseCallback callback, BaseNotification bean) {
        this.callback = callback;
        this.bean = bean;
    }

    @Override
    public void run() {
        if (callback instanceof TCPSocket.SCTCPCallback) {
            if (bean instanceof OnConnectionStateChanged) {
                ((TCPSocket.SCTCPCallback) callback).onManagedConnectState(bean.getId(), ((OnConnectionStateChanged) bean).getConnectState());
            } else if (bean instanceof OnTCPDataArrived) {
                ((TCPSocket.SCTCPCallback) callback).onDataArrived(bean.getId(), ((OnTCPDataArrived) bean).getObj());
            } else if (bean instanceof OnUnmanagedTCPSocketCreated) {
                ((TCPSocket.SCTCPCallback) callback).onUnmangedCreated(bean.getId(), ((OnUnmanagedTCPSocketCreated) bean).getSocket());
            } else if (bean instanceof OnHeartbeat) {
                ((TCPSocket.SCTCPCallback) callback).onHeartbeat(bean.getId());
            } else {
                System.err.println("Notification NotSupport TCP");
            }
        } else if (callback instanceof UDPSocket.SCUDPCallback) {
            if (bean instanceof OnUDPDataArrived) {
                ((UDPSocket.SCUDPCallback) callback).onDataArrived(((OnUDPDataArrived) bean).getHost(), ((OnUDPDataArrived) bean).getPort(), ((OnUDPDataArrived) bean).getObj());
            } else {
                System.err.println("Notification NotSupport UDP");
            }
        } else {
            System.err.println("Not Support");
        }
    }
}