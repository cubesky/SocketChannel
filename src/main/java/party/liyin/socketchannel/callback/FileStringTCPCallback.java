package party.liyin.socketchannel.callback;

import party.liyin.socketchannel.transporthelper.FileTrasportHelper;

import java.io.IOException;
import java.net.Socket;

public abstract class FileStringTCPCallback extends StringTCPCallback {
    @Override
    public void onUnmangedCreated(long id, String tag, Socket socket) {
        try {
            FileTrasportHelper.FileTransportBean bean = FileTrasportHelper.handleFileTransport(id, tag, socket);
            if (bean.isHandled()) {
                if (bean.isNeedOperation()) {
                    onFileArrived(bean.getFilename(), bean.getFileLength(), new FileTask(bean));
                }
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract void onFileArrived(String filename, long fileLength, FileTask fileTask);
}
