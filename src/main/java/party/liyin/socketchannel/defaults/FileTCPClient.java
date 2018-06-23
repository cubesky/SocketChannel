package party.liyin.socketchannel.defaults;

import party.liyin.socketchannel.TCPClient;
import party.liyin.socketchannel.callback.TCPSocket;
import party.liyin.socketchannel.transporthelper.FileTrasportHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;

public class FileTCPClient extends TCPClient {
    public FileTCPClient(InetSocketAddress address, TCPSocket.SCTCPCallback scTCPCallback) {
        super(address, scTCPCallback);
    }

    public FileTCPClient(InetSocketAddress address, TCPSocket.SCTCPCallback scTCPCallback, boolean manuallyMode) {
        super(address, scTCPCallback, manuallyMode);
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
