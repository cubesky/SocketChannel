package party.liyin.socketchannel;

class TCPDataArrivedNotification extends Notification {
    private byte[] obj;

    TCPDataArrivedNotification(long id, byte[] obj) {
        this.id = id;
        this.obj = obj;
    }

    byte[] getObj() {
        return obj;
    }
}