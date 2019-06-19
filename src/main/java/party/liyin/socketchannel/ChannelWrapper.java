package party.liyin.socketchannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.charset.StandardCharsets;

public class ChannelWrapper {
    public static class SystemIn {
        private static Pipe.SinkChannel sysinSink = null;
        private static Pipe.SourceChannel sysinSource = null;
        private static Thread thread = null;

        private static void init() throws IOException {
            Pipe pipe = Pipe.open();
            sysinSink = pipe.sink();
            sysinSource = pipe.source();
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
                        while (!Thread.interrupted()) {
                            try {
                                if (reader.ready())
                                    sysinSink.write(ByteBuffer.wrap(reader.readLine().getBytes(StandardCharsets.UTF_8)));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sysinSink = null;
                    sysinSource = null;
                    thread = null;
                }
            });
            thread.setDaemon(true);
            thread.start();
            sysinSource.configureBlocking(false);
        }

        public static Pipe.SourceChannel getSystemInChannel() throws IOException {
            if (sysinSource == null) {
                SystemIn.init();
            }
            return SystemIn.sysinSource;
        }

        public static void stopWrapper() {
            if (sysinSource != null) {
                thread.interrupt();
            }
        }
    }

}
