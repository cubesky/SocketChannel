package party.liyin.socketchannel.notification;

public class OnTCPDataArrived extends BaseNotification {
    private byte[] obj;

    public OnTCPDataArrived(long id, byte[] obj) {
        this.id = id;
        this.obj = obj;
    }

    byte[] getObj() {
        return obj;
    }
}