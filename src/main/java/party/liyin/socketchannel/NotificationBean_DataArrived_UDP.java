package party.liyin.socketchannel;

public class NotificationBean_DataArrived_UDP extends NotificationBean {
    private String host;
    private int port;
    private byte[] obj;

    public NotificationBean_DataArrived_UDP(String host, int port, byte[] obj) {
        this.host = host;
        this.port = port;
        this.obj = obj;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public byte[] getObj() {
        return obj;
    }
}
