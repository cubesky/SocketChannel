package party.liyin.socketchannel.defaults;

import party.liyin.socketchannel.callback.TCPSocket;
import party.liyin.socketchannel.transporthelper.FileTrasportHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;

public class FileStringTCPServer extends StringTCPServer {
    public FileStringTCPServer(InetSocketAddress address, TCPSocket.SCTCPCallback scTCPCallback) throws IOException {
        super(address, scTCPCallback);
    }

    public FileStringTCPServer(InetSocketAddress address, TCPSocket.SCTCPCallback scTCPCallback, boolean fullyManagement) throws IOException {
        super(address, scTCPCallback, fullyManagement);
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
