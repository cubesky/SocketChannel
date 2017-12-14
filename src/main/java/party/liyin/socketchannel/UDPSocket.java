package party.liyin.socketchannel;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UDPSocket implements Closeable {


    private MessageQueue messageQueue = new MessageQueue();
    private DatagramChannel datagramChannel;
    private Selector selector;
    private int port;
    private boolean isStarted = false;
    private SCUDPCallback scUDPCallback;
    private ExecutorService service = Executors.newFixedThreadPool(1);

    /**
     * Create an UDP Channel
     *
     * @param port           UDP Listen Port
     * @param scUDPCallback UDP Callback
     * @throws IOException
     */
    public UDPSocket(int port, SCUDPCallback scUDPCallback) throws IOException {
        this.port = port;
        this.scUDPCallback = scUDPCallback;
    }

    /**
     * Start A NIO UDP Socket
     *
     * @throws IOException
     */
    public void start() throws IOException {
        if (isStarted) throw new IOException("Already Started");
        isStarted = true;
        datagramChannel = DatagramChannel.open();
        datagramChannel.bind(new InetSocketAddress(port));
        datagramChannel.configureBlocking(false);
        selector = Selector.open();
        datagramChannel.register(selector, SelectionKey.OP_READ);
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
                            System.out.println("Recieve");
                            DatagramChannel datagramChannel = (DatagramChannel) key.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(2048);
                            try {
                                InetSocketAddress address = (InetSocketAddress) datagramChannel.receive(buffer);
                                byte[] resultArray = buffer.array();
                                messageQueue.offer(new NotificationTask(scUDPCallback, new UDPDataArrivedNotification(address.getHostString(), address.getPort(), resultArray)));
                            } catch (IOException ignored) {
                            }
                        }
                        selectionKeyIterator.remove();
                    }
                } catch (Exception ignore) {
                } finally {
                    try {
                        datagramChannel.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        });
    }

    /**
     * Stop All
     */
    public void stop() {
        try {
            selector.close();
        } catch (IOException ignored) {
        }
        try {
            datagramChannel.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * Send Message through Managed UDP Socket
     *
     * @param ip   Host
     * @param port Port
     * @param obj  Data Byte Array
     * @throws IOException
     */
    public void sendMessage(String ip, int port, byte[] obj) throws IOException {
        if (obj.length > 2048) throw new IOException("Too Large, Data must under 2048 bytes.");
        DatagramChannel channel = DatagramChannel.open();
        channel.send(ByteBuffer.wrap(obj), new InetSocketAddress(ip, port));
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

    /**
     * UDP Callback
     */
    public interface SCUDPCallback extends SCBaseCallback {
        /**
         * When Data Arrived
         *
         * @param ip   Host
         * @param port Port
         * @param obj  Data Byte Array
         */
        void onDataArrived(String ip, int port, byte[] obj);
    }
}
