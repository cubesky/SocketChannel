package party.liyin.socketchannel;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPClient implements Closeable {
    private MessageQueue messageQueue = new MessageQueue();
    private TCPSocket.SCTCPCallback scTCPCallback;
    private SocketChannel socketChannel;
    private InetSocketAddress address;
    private Selector selector;
    private ExecutorService service = Executors.newFixedThreadPool(1);
    private boolean isStarted = false;

    /**
     * TCP NIO Client
     *
     * @param address        Address and Port for TCP Server
     * @param scTCPCallback TCP Client Callback
     */
    public TCPClient(InetSocketAddress address, TCPSocket.SCTCPCallback scTCPCallback) {
        this.address = address;
        this.scTCPCallback = scTCPCallback;
    }

    /**
     * Start Non-block TCP Client
     *
     * @throws IOException
     */
    public void start() throws IOException {
        if (isStarted) throw new IOException("Already Started");
        isStarted = true;
        try {
            socketChannel = SocketChannel.open(address);
        } catch (IOException connect) {
            socketChannel = null;
            throw connect;
        }
        messageQueue.offer(new NotificationTask(scTCPCallback, new ConnectionStateNotification(0, TCPSocket.ConnectState.CONNECT)));
        socketChannel.configureBlocking(false);
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_READ);
        service.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        int readyChannels = selector.select();
                        if (readyChannels == 0) continue;
                        Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
                        while (selectionKeyIterator.hasNext()) {
                            SelectionKey key = selectionKeyIterator.next();
                            if (key.isReadable()) {
                                SocketChannel socketChannel = (SocketChannel) key.channel();
                                ByteBuffer buffer = ByteBuffer.allocate(1024);
                                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                try {
                                    int length;
                                    while ((length = socketChannel.read(buffer)) > 0) {
                                        buffer.flip();
                                        outputStream.write(buffer.array(), 0, length);
                                        buffer.clear();
                                    }
                                    if (outputStream.size() == 0) throw new IOException("Closed");
                                } catch (IOException e) {
                                    key.cancel();
                                    selectionKeyIterator.remove();
                                    throw new IOException("Disconnect");
                                }
                                byte[] resultArray = outputStream.toByteArray();
                                byte[] resultData = new byte[resultArray.length - 1];
                                byte cmd = resultArray[0];
                                System.arraycopy(resultArray, 1, resultData, 0, resultArray.length - 1);
                                if (cmd == 0) {
                                    messageQueue.offer(new NotificationTask(scTCPCallback, new TCPDataArrivedNotification(0, resultData)));
                                } else if (cmd == 1) {
                                    final SocketChannel fsocketChannel = socketChannel;
                                    final String port = new String(resultData);
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Socket socket = new Socket();
                                            try {
                                                socket.connect(new InetSocketAddress(fsocketChannel.socket().getInetAddress().getHostAddress(), Integer.valueOf(port)));
                                                messageQueue.offer(new NotificationTask(scTCPCallback, new UnmanagedTCPSocketCreatedNotification(0, socket)));
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }).start();
                                } else {
                                    System.err.println("Not Support");
                                }
                            }
                            selectionKeyIterator.remove();
                        }
                    }
                } catch (Exception ignore) {
                    messageQueue.offer(new NotificationTask(scTCPCallback, new ConnectionStateNotification(0, TCPSocket.ConnectState.DISCONNECT)));
                } finally {
                    try {
                        socketChannel.close();
                    } catch (IOException ignored) {
                    }
                    messageQueue.offer(new NotificationTask(scTCPCallback, new ConnectionStateNotification(0, TCPSocket.ConnectState.CLOSED)));
                }
            }
        });
    }

    /**
     * Close All
     *
     * @throws IOException
     */
    public void stop() throws IOException {
        try {
            selector.close();
        } catch (IOException ignored) {
        }
        try {
            socketChannel.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * Request an Unmanaged Socket in IO Mode
     */
    public void createUnmanagedSocket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ByteBuffer buffer = ByteBuffer.allocate(1);
                buffer.put(new byte[]{1});
                buffer.flip();
                try {
                    socketChannel.write(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Send Data through Managed NIO Channel
     *
     * @param obj Byte Array you want to send.
     */
    public void sendMessage(byte[] obj) {
        final byte[] fobj = obj;
        new Thread(new Runnable() {
            @Override
            public void run() {
                ByteBuffer buffer = ByteBuffer.allocate(fobj.length + 1);
                buffer.put(new byte[]{0});
                buffer.put(fobj);
                buffer.flip();
                try {
                    socketChannel.write(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Stop
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        stop();
    }
}
