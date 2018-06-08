package party.liyin.socketchannel.callback;

import java.io.UnsupportedEncodingException;

public abstract class StringUDPCallback extends DefaultUDPCallback {
    @Override
    public void onDataArrived(String ip, int port, byte[] obj) {
        try {
            onDataArrived(ip, port, new String(obj, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    abstract void onDataArrived(String ip, int port, String obj);
}