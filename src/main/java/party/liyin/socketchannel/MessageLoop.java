package party.liyin.socketchannel;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageLoop {
    private BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    /**
     * MessageQueue to Notify Other Thread in Async
     */
    public MessageLoop() {
        Thread messageThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        new Thread(queue.take()).start();
                    } catch (Exception e) {
                        System.err.println("MessageQueue Task Exception");
                    }
                }
            }
        });
        messageThread.setDaemon(true);
        messageThread.start();
    }

    /**
     * Offer a Task
     *
     * @param task Async Task you want to
     */
    public void offer(Runnable task) {
        queue.offer(task);
    }
}