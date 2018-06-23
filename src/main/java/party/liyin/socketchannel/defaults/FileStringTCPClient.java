package party.liyin.socketchannel.defaults;

import party.liyin.socketchannel.callback.TCPSocket;
import party.liyin.socketchannel.transporthelper.FileTrasportHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;

public class FileStringTCPClient extends StringTCPClient {
    public FileStringTCPClient(InetSocketAddress address, TCPSocket.SCTCPCallback scTCPCallback) {
        super(address, scTCPCallback);
    }

    public FileStringTCPClient(InetSocketAddress address, TCPSocket.SCTCPCallback scTCPCallback, boolean fullyManagement) {
        super(address, scTCPCallback, fullyManagement);
    }

    /**
     * Send file to Server
     *
     * @param file file which want to send
     * @throws FileNotFoundException
     */
    public void sendFile(File file) throws FileNotFoundException {
        FileTrasportHelper.send(this, file);
    }
}
