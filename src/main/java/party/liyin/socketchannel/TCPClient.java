package party.liyin.socketchannel;

import party.liyin.socketchannel.callback.TCPSocket;
import party.liyin.socketchannel.notification.*;

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
    private MessageLoop messageLoop = new MessageLoop();
    private TCPSocket.SCTCPCallback scTCPCallback;
    private SocketChannel socketChannel;
    private InetSocketAddress address;
    private Selector selector;
    private ExecutorService service = Executors.newFixedThreadPool(1);
    private boolean manuallyMode = false;
    private boolean isStarted = false;

    /**
     * TCP NIO Client
     *
     * @param address        Address and Port for TCP Server
     * @param scTCPCallback TCP Client Callback
     */
    public TCPClient(InetSocketAddress address, TCPSocket.SCTCPCallback scTCPCallback) {
        this(address, scTCPCallback, false);
    }

    /**
     * TCP NIO Client
     *
     * @param address       Address and Port for TCP Server
     * @param scTCPCallback TCP Client Callback
     */
    public TCPClient(InetSocketAddress address, TCPSocket.SCTCPCallback scTCPCallback, boolean manuallyMode) {
        this.address = address;
        this.scTCPCallback = scTCPCallback;
        this.manuallyMode = manuallyMode;
    }

    /**
     * Start Non-block TCP Client in new Thread
     *
     * @param startCallback TCP Connect Result Callback
     */
    public void startAsync(final TCPSocket.SCTCPConnectResultCallback startCallback) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    start();
                    if (startCallback != null) startCallback.onConnect();
                } catch (IOException e) {
                    if (startCallback != null) startCallback.onError();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
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
        messageLoop.offer(new NotificationTask(scTCPCallback, new OnConnectionStateChanged(0, TCPSocket.ConnectState.CONNECT)));
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
                                if (manuallyMode) {
                                    messageLoop.offer(new NotificationTask(scTCPCallback, new OnTCPDataArrived(0, resultArray)));
                                } else {
                                    byte[] resultData = new byte[resultArray.length - 1];
                                    byte cmd = resultArray[0];
                                    System.arraycopy(resultArray, 1, resultData, 0, resultArray.length - 1);
                                    if (cmd == 0) {
                                        messageLoop.offer(new NotificationTask(scTCPCallback, new OnTCPDataArrived(0, resultData)));
                                    } else if (cmd == 1) {
                                        final SocketChannel fsocketChannel = socketChannel;
                                        final String umpdata = new String(resultData, "UTF-8");
                                        String[] portAndTag = umpdata.split("\\|", 2);
                                        final int port = Integer.valueOf(portAndTag[0]);
                                        final String tag = portAndTag.length == 1 ? "" : portAndTag[1];
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    Socket socket = SocketChannel.open().socket();
                                                    socket.connect(new InetSocketAddress(fsocketChannel.socket().getInetAddress().getHostAddress(), port));
                                                    messageLoop.offer(new NotificationTask(scTCPCallback, new OnUnmanagedTCPSocketCreated(0, tag, socket)));
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }).start();
                                    } else if (cmd == 2) {
                                        sendHeartbeat();
                                        messageLoop.offer(new NotificationTask(scTCPCallback, new OnHeartbeat(0)));
                                    } else {
                                        System.err.println("Not Support");
                                    }
                                }

                            }
                            selectionKeyIterator.remove();
                        }
                    }
                } catch (Exception ignore) {
                    messageLoop.offer(new NotificationTask(scTCPCallback, new OnConnectionStateChanged(0, TCPSocket.ConnectState.DISCONNECT)));
                } finally {
                    try {
                        socketChannel.close();
                    } catch (IOException ignored) {
                    }
                    messageLoop.offer(new NotificationTask(scTCPCallback, new OnConnectionStateChanged(0, TCPSocket.ConnectState.CLOSED)));
                }
            }
        });
    }

    /**
     * Close All
     *
     * @throws IOException Close Error
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
     *
     * @param tag Tag for Socket
     */
    public void createUnmanagedSocket(String tag) {
        if (manuallyMode) return;
        final String finalTag = (tag == null) ? "" : tag;
        new Thread(new Runnable() {
            @Override
            public void run() {
                ByteBuffer buffer = ByteBuffer.allocate(1 + finalTag.getBytes().length);
                buffer.put(new byte[]{1});
                buffer.put(finalTag.getBytes());
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
     * Request an Unmanaged Socket in IO Mode with no tag
     */
    public void createUnmanagedSocket() {
        createUnmanagedSocket("");
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
                int length = fobj.length;
                if (manuallyMode) {
                    length = length - 1;
                }
                ByteBuffer buffer = ByteBuffer.allocate(length + 1);
                if (!manuallyMode) buffer.put(new byte[]{0});
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
     * Send Heartbeat
     */
    private void sendHeartbeat() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (manuallyMode) {
                    return;
                }
                ByteBuffer buffer = ByteBuffer.allocate(1);
                buffer.put(new byte[]{2});
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
