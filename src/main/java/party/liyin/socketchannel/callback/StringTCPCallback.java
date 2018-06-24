package party.liyin.socketchannel.callback;

import java.io.UnsupportedEncodingException;

public abstract class StringTCPCallback extends DefaultTCPCallback {
    @Override
    public void onDataArrived(long id, byte[] obj) {
        try {
            onDataArrived(id, new String(obj, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public abstract void onDataArrived(long id, String obj);
}