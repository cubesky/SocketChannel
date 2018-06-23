package party.liyin.socketchannel;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import party.liyin.socketchannel.callback.TCPSocket;
import party.liyin.socketchannel.notification.*;

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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TCPServer implements Closeable {
    private MessageLoop messageLoop = new MessageLoop();
    private HashBiMap<SocketChannel, Long> peerMap = HashBiMap.create();
    private TCPSocket.SCTCPCallback scTCPCallback;
    private ServerSocketChannel serverSocketChannel;
    private InetSocketAddress address;
    private Selector selector;
    private ExecutorService service = Executors.newFixedThreadPool(1);
    private boolean manuallyMode = false;
    private boolean isStarted = false;
    private TCPSocket.SCAuthCallback connAuthCallback = null;
    private ScheduledExecutorService heartbeatExecutor = Executors.newScheduledThreadPool(1);

    /**
     * TCP NIO Server
     *
     * @param address       Bind IP and Port
     * @param scTCPCallback TCP Callback
     * @throws IOException
     */
    public TCPServer(InetSocketAddress address, TCPSocket.SCTCPCallback scTCPCallback) throws IOException {
        this(address, scTCPCallback, false);
    }

    /**
     * TCP NIO Server
     *
     * @param address       Bind IP and Port
     * @param scTCPCallback TCP Callback
     * @throws IOException
     */
    public TCPServer(InetSocketAddress address, TCPSocket.SCTCPCallback scTCPCallback, boolean manuallyMode) throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        this.address = address;
        this.scTCPCallback = scTCPCallback;
        this.manuallyMode = manuallyMode;
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
                                if (connAuthCallback != null) {
                                    if (!connAuthCallback.onNewSocketAuth(socketChannel.socket())) {
                                        socketChannel.finishConnect();
                                        socketChannel.close();
                                        continue;
                                    }
                                }
                                socketChannel.configureBlocking(false);
                                socketChannel.register(selector, SelectionKey.OP_READ);
                                long id = Utils.getNewUniqueId(peerMap);
                                peerMap.put(socketChannel, id);
                                messageLoop.offer(new NotificationTask(scTCPCallback, new OnConnectionStateChanged(id, TCPSocket.ConnectState.CONNECT)));
                            } else if (key.isReadable()) {
                                SocketChannel socketChannel = (SocketChannel) key.channel();
                                Long id = peerMap.get(socketChannel);
                                if (id == null) {
                                    System.err.println("ID Error");
                                    selectionKeyIterator.remove();
                                    continue;
                                }
                                ByteBuffer buffer = ByteBuffer.allocate(2048);
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
                                    messageLoop.offer(new NotificationTask(scTCPCallback, new OnConnectionStateChanged(id, TCPSocket.ConnectState.DISCONNECT)));
                                    selectionKeyIterator.remove();
                                    continue;
                                }
                                byte[] resultArray = outputStream.toByteArray();
                                if (manuallyMode) {
                                    messageLoop.offer(new NotificationTask(scTCPCallback, new OnTCPDataArrived(id, resultArray)));
                                } else {
                                    byte[] resultData = new byte[resultArray.length - 1];
                                    byte cmd = resultArray[0];
                                    System.arraycopy(resultArray, 1, resultData, 0, resultArray.length - 1);
                                    if (cmd == 0) { // Data
                                        messageLoop.offer(new NotificationTask(scTCPCallback, new OnTCPDataArrived(id, resultData)));
                                    } else if (cmd == 1) { // UnmanagedSocket
                                        final long fid = id;
                                        final String tag = new String(resultData, "UTF-8");
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    final ServerSocketChannel unmanagedServerSocketChannel = ServerSocketChannel.open();
                                                    final ServerSocket serverSocket = unmanagedServerSocketChannel.socket();
                                                    serverSocket.bind(new InetSocketAddress("0.0.0.0", 0));
                                                    byte[] port = String.valueOf(serverSocket.getLocalPort()).getBytes();
                                                    ByteBuffer buffer = ByteBuffer.allocate(1 + port.length + 1 + tag.getBytes().length);
                                                    buffer.put(new byte[]{1});
                                                    buffer.put(port);
                                                    buffer.put("|".getBytes());
                                                    buffer.put(tag.getBytes());
                                                    buffer.flip();
                                                    peerMap.inverse().get(fid).write(buffer);
                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            try {
                                                                Socket socket = unmanagedServerSocketChannel.accept().socket();
                                                                serverSocket.close();
                                                                messageLoop.offer(new NotificationTask(scTCPCallback, new OnUnmanagedTCPSocketCreated(fid, tag, socket)));
                                                            } catch (Exception ignored) {
                                                            }
                                                        }
                                                    }).start();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }).start();
                                    } else if (cmd == 2) { //HeartBeat
                                        messageLoop.offer(new NotificationTask(scTCPCallback, new OnHeartbeat(id)));
                                    } else {
                                        System.err.println("Not Support");
                                    }
                                }
                            }
                            selectionKeyIterator.remove();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    messageLoop.offer(new NotificationTask(scTCPCallback, new OnConnectionStateChanged(0, TCPSocket.ConnectState.CLOSED)));
                }
            }
        });
        if (!manuallyMode) {
            heartbeatExecutor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        sendHeartbeat();
                    } catch (Exception ignored) {
                    }
                }
            }, 1, 2, TimeUnit.MINUTES);
        }
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
     * Set Auth Callback
     *
     * @param connAuthCallback instance of TCPSocket.SCAuthCallback
     */
    public void setConnAuthCallback(TCPSocket.SCAuthCallback connAuthCallback) {
        this.connAuthCallback = connAuthCallback;
    }

    /**
     * Stop all
     */
    public void stop() {
        try {
            if (!manuallyMode) {
                heartbeatExecutor.shutdownNow();
                heartbeatExecutor = Executors.newScheduledThreadPool(1);
            }
        } catch (Exception ignore) {
        }
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
     * Create an Unmanaged Socket
     *
     * @param id id for client
     * @param tag tag for socket
     */
    public void createUnmanagedSocket(long id, String tag) {
        if (manuallyMode) return;
        final byte[] finalTag = (tag == null) ? "".getBytes() : tag.getBytes();
        final long fid = id;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final ServerSocketChannel unmanagedServerSocketChannel = ServerSocketChannel.open();
                    final ServerSocket serverSocket = unmanagedServerSocketChannel.socket();
                    serverSocket.bind(new InetSocketAddress("0.0.0.0", 0));
                    byte[] port = String.valueOf(serverSocket.getLocalPort()).getBytes();
                    ByteBuffer buffer = ByteBuffer.allocate(1 + port.length + 1 + finalTag.length);
                    buffer.put(new byte[]{1});
                    buffer.put(port);
                    buffer.put("|".getBytes());
                    buffer.put(finalTag);
                    buffer.flip();
                    peerMap.inverse().get(fid).write(buffer);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Socket socket = serverSocket.accept();
                                messageLoop.offer(new NotificationTask(scTCPCallback, new OnUnmanagedTCPSocketCreated(fid, new String(finalTag, "UTF-8"), socket)));
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
     * Create an Unmanaged Socket with no tag
     *
     * @param id for client
     */
    public void createUnmanagedSocket(long id) {
        createUnmanagedSocket(id, "");
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
        int length = obj.length;
        if (manuallyMode) {
            length = length - 1;
        }
        final ByteBuffer buffer = ByteBuffer.allocate(length + 1);
        if (!manuallyMode) buffer.put(new byte[]{0});
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
     * Send Data Through a managed Channel
     */
    private void sendHeartbeat() {
        if (manuallyMode) {
            return;
        }
        final ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put(new byte[]{2});
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
     * Return if this instance is in manually mode
     *
     * @return
     */
    public boolean isManuallyMode() {
        return manuallyMode;
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