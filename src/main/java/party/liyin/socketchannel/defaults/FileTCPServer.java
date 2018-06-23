package party.liyin.socketchannel.defaults;

import party.liyin.socketchannel.TCPServer;
import party.liyin.socketchannel.callback.TCPSocket;
import party.liyin.socketchannel.transporthelper.FileTrasportHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;

public class FileTCPServer extends TCPServer {
    public FileTCPServer(InetSocketAddress address, TCPSocket.SCTCPCallback scTCPCallback) throws IOException {
        super(address, scTCPCallback);
    }

    public FileTCPServer(InetSocketAddress address, TCPSocket.SCTCPCallback scTCPCallback, boolean manuallyMode) throws IOException {
        super(address, scTCPCallback, manuallyMode);
    }

    /**
     * Send file to client
     *
     * @param id   Client id
     * @param file file which want to send
     * @throws FileNotFoundException
     */
    public void sendFile(long id, File file) throws FileNotFoundException {
        FileTrasportHelper.send(this, id, file);
    }
}
