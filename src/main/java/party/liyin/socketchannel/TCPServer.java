package party.liyin.socketchannel;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer implements Closeable {
    private MessageQueue messageQueue = new MessageQueue();
    private HashBiMap<SocketChannel, Long> peerMap = HashBiMap.create();
    private TCPSocket.SkyTCPCallback skyTCPCallback;
    private ServerSocketChannel serverSocketChannel;
    private InetSocketAddress address;
    private Selector selector;
    private ExecutorService service = Executors.newFixedThreadPool(1);
    private boolean isStarted = false;

    /**
     * TCP NIO Server
     *
     * @param address        Bind IP and Port
     * @param skyTCPCallback TCP Callback
     * @throws IOException
     */
    public TCPServer(InetSocketAddress address, TCPSocket.SkyTCPCallback skyTCPCallback) throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        this.address = address;
        this.skyTCPCallback = skyTCPCallback;
    }

    /**
     * Start a NIO TCP Server
     *
     * @throws IOException
     */
    public void start() throws IOException {
        if (isStarted) throw new IOException("Already Started");
        isStarted = true;
        peerMap.clear();
        serverSocketChannel.socket().bind(address);
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
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
                            if (key.isAcceptable()) {
                                SocketChannel socketChannel = serverSocketChannel.accept();
                                socketChannel.configureBlocking(false);
                                socketChannel.register(selector, SelectionKey.OP_READ);
                                long id = Utils.getNewUniqueId(peerMap);
                                peerMap.put(socketChannel, id);
                                messageQueue.offer(new NotificationRunnable(skyTCPCallback, new NotificationBean_ConnectionState(id, TCPSocket.ConnectState.CONNECT)));
                            } else if (key.isReadable()) {
                                SocketChannel socketChannel = (SocketChannel) key.channel();
                                Long id = peerMap.get(socketChannel);
                                if (id == null) {
                                    System.err.println("ID Error");
                                    selectionKeyIterator.remove();
                                    continue;
                                }
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
                                    peerMap.remove(socketChannel);
                                    messageQueue.offer(new NotificationRunnable(skyTCPCallback, new NotificationBean_ConnectionState(id, TCPSocket.ConnectState.DISCONNECT)));
                                    selectionKeyIterator.remove();
                                    continue;
                                }
                                byte[] resultArray = outputStream.toByteArray();
                                byte[] resultData = new byte[resultArray.length - 1];
                                byte cmd = resultArray[0];
                                System.arraycopy(resultArray, 1, resultData, 0, resultArray.length - 1);
                                if (cmd == 0) {
                                    messageQueue.offer(new NotificationRunnable(skyTCPCallback, new NotificationBean_DataArrived(id, resultData)));
                                } else if (cmd == 1) {
                                    final long fid = id;
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                final ServerSocket serverSocket = new ServerSocket(0);
                                                byte[] port = String.valueOf(serverSocket.getLocalPort()).getBytes();
                                                ByteBuffer buffer = ByteBuffer.allocate(port.length + 1);
                                                buffer.put(new byte[]{1});
                                                buffer.put(port);
                                                buffer.flip();
                                                peerMap.inverse().get(fid).write(buffer);
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        try {
                                                            Socket socket = serverSocket.accept();
                                                            serverSocket.close();
                                                            messageQueue.offer(new NotificationRunnable(skyTCPCallback, new NotificationBean_UnmanagedCreated(fid, socket)));
                                                        } catch (Exception ignored) {
                                                        }
                                                    }
                                                }).start();
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
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                } finally {
                    messageQueue.offer(new NotificationRunnable(skyTCPCallback, new NotificationBean_ConnectionState(0, TCPSocket.ConnectState.CLOSED)));
                }
            }
        });
    }

    /**
     * Check if id is same host
     *
     * @param id1 First ID
     * @param id2 Second ID
     * @return return true if ID is the same host
     */
    public boolean isSameHost(long id1, long id2) {
        BiMap<Long, SocketChannel> inverse = peerMap.inverse();
        String host1 = inverse.get(id1).socket().getInetAddress().getHostAddress();
        String host2 = inverse.get(id2).socket().getInetAddress().getHostAddress();
        return host1.equals(host2);
    }

    /**
     * Stop all
     */
    public void stop() {
        try {
            selector.close();
        } catch (IOException ignored) {
        }
        try {
            serverSocketChannel.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * Close a client connection
     *
     * @param id
     * @throws IOException
     */
    public void closeChannel(long id) throws IOException {
        peerMap.inverse().get(id).close();
    }

    /**
     * Create a Managed Socket
     *
     * @param id
     */
    public void createUnmanagedSocket(long id) {
        final long fid = id;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final ServerSocket serverSocket = new ServerSocket(0);
                    byte[] port = String.valueOf(serverSocket.getLocalPort()).getBytes();
                    ByteBuffer buffer = ByteBuffer.allocate(port.length + 1);
                    buffer.put(new byte[]{1});
                    buffer.put(port);
                    buffer.flip();
                    peerMap.inverse().get(fid).write(buffer);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Socket socket = serverSocket.accept();
                                messageQueue.offer(new NotificationRunnable(skyTCPCallback, new NotificationBean_UnmanagedCreated(fid, socket)));
                            } catch (Exception ignored) {
                            }
                        }
                    }).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Send Data Through a managed Channel
     *
     * @param id  ID of Client
     * @param obj Data Byte Attay
     * @throws IOException
     */
    public void sendMessage(long id, byte[] obj) throws IOException {
        final long fid = id;
        final ByteBuffer buffer = ByteBuffer.allocate(obj.length + 1);
        buffer.put(new byte[]{0});
        buffer.put(obj);
        if (id == 0) {
            final byte[] boardcastarray = buffer.array();
            for (final SocketChannel socketChannel : peerMap.inverse().values()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            socketChannel.write(ByteBuffer.wrap(boardcastarray));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        } else {
            buffer.flip();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        peerMap.inverse().get(fid).write(buffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    /**
     * Send Broadcast to All Client
     *
     * @param obj Data Byte Array
     * @throws IOException
     */
    public void sendBroadcast(byte[] obj) throws IOException {
        sendMessage(0, obj);
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