package party.liyin.socketchannel.transporthelper;

import party.liyin.socketchannel.TCPClient;
import party.liyin.socketchannel.TCPServer;
import party.liyin.socketchannel.exception.OperationNotSupportException;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.UUID;

public class FileTrasportHelper {
    private static HashMap<String, File> fileInfoList = new HashMap<>();

    /**
     * Send file to client
     *
     * @param server SocketChannel Server instance
     * @param id     ID of the client
     * @param file   file which want to send
     * @throws FileNotFoundException File not found
     */
    public static void send(TCPServer server, long id, File file) throws FileNotFoundException {
        if (server.isManuallyMode())
            throw new OperationNotSupportException("Transport Helper can not work with manual mode instance.");
        if (file == null || !file.exists()) throw new FileNotFoundException();
        String transportId = UUID.randomUUID().toString();
        fileInfoList.put(transportId, file);
        server.createUnmanagedSocket(id, "FileTransport|" + transportId + "|" + file.getName() + "|" + file.length());
    }

    /**
     * Send file to server
     *
     * @param client SocketChannel Client instance
     * @param file   file which want to send
     * @throws FileNotFoundException File not found
     */
    public static void send(TCPClient client, File file) throws FileNotFoundException {
        if (client.isManuallyMode())
            throw new OperationNotSupportException("Transport Helper can not work with manual mode instance.");
        if (file == null || !file.exists()) throw new FileNotFoundException();
        String transportId = UUID.randomUUID().toString();
        fileInfoList.put(transportId, file);
        client.createUnmanagedSocket("FileTransport|" + transportId + "|" + file.getName() + "|" + file.length());
    }

    /**
     * Handle File Transport. Call this method in onUnmanagedSocketCreated
     *
     * @param id     param in onUnmanagedSocketCreated
     * @param tag    param in onUnmanagedSocketCreated
     * @param socket param in onUnmanagedSocketCreated
     * @return Transport Info Bean
     * @throws IOException
     */
    public static FileTransportBean handleFileTransport(long id, String tag, Socket socket) throws IOException {
        if (!tag.startsWith("FileTransport|")) return new FileTransportBean(false, false, null);
        String[] command = tag.split("\\|", 4);
        File info = fileInfoList.get(command[1]);
        if (info == null) { //Receiver
            SocketChannel channel = socket.getChannel();
            return new FileTransportBean(true, true, command[2], channel, Long.valueOf(command[3]));
        } else { //Sender
            fileInfoList.remove(command[1]);
            FileChannel fileChannel;
            if (System.getProperty("java.version").startsWith("1.6")) {
                fileChannel = new FileInputStream(info).getChannel();
            } else {
                fileChannel = FileChannel.open(info.toPath(), StandardOpenOption.READ);
            }
            fileChannel.transferTo(0, Long.valueOf(command[3]), socket.getChannel());
            fileChannel.close();
            return new FileTransportBean(true, false, command[2]);
        }
    }

    /**
     * Save file from info bean
     *
     * @param bean   info bean
     * @param output file output path
     * @throws IOException
     */
    public static void saveFileFromBean(FileTransportBean bean, File output) throws IOException {
        if (bean.isHandled() && bean.isNeedOperation() && bean.getFilename() != null) {
            FileChannel fileChannel;
            if (System.getProperty("java.version").startsWith("1.6")) {
                fileChannel = new FileOutputStream(output).getChannel();
            } else {
                fileChannel = FileChannel.open(output.toPath(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            }
            fileChannel.transferFrom(bean.getChannel(), 0, bean.getFileLength());
            fileChannel.close();
            bean.getChannel().close();
        }
    }

    /**
     * Get file byte array. Warning: this method can not work with large file.
     *
     * @param bean info bean
     * @return Byte Array
     * @throws IOException
     */
    public static byte[] getByteArrayFromBean(FileTransportBean bean) throws IOException {
        if (bean.isHandled() && bean.isNeedOperation() && bean.getFilename() != null) {
            ByteBuffer buffer = ByteBuffer.allocate((int) bean.getFileLength());
            bean.getChannel().read(buffer);
            bean.getChannel().close();
            return buffer.array();
        }
        return new byte[]{};
    }

    public static class FileTransportBean {
        private String filename;
        private long fileLength;
        private boolean isHandled;
        private boolean needOperation;
        private SocketChannel channel = null;

        private FileTransportBean(boolean isHandled, boolean needOperation, String filename) {
            this.isHandled = isHandled;
            this.filename = filename;
            this.needOperation = needOperation;
        }

        private FileTransportBean(boolean isHandled, boolean needOperation, String filename, SocketChannel channel, long fileLength) {
            this(isHandled, needOperation, filename);
            this.channel = channel;
            this.fileLength = fileLength;
        }

        public boolean isHandled() {
            return isHandled;
        }

        public String getFilename() {
            return filename;
        }

        public boolean isNeedOperation() {
            return needOperation;
        }

        public SocketChannel getChannel() {
            return channel;
        }

        public long getFileLength() {
            return fileLength;
        }

        @Override
        public String toString() {
            return "File: " + filename + "(" + fileLength + ")\n" + " isHandled: " + isHandled + "\n needOperation: " + needOperation + "\n";
        }
    }
}
