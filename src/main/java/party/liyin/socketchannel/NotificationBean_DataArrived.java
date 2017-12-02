package party.liyin.socketchannel;

class NotificationBean_DataArrived extends NotificationBean {
    private byte[] obj;

    NotificationBean_DataArrived(long id, byte[] obj) {
        this.id = id;
        this.obj = obj;
    }

    byte[] getObj() {
        return obj;
    }
}