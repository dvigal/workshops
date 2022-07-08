package practicum.concurrency.lifecycle;

import java.time.Duration;
import java.util.stream.IntStream;

import static practicum.concurrency.Utils.printThread;
import static practicum.concurrency.Utils.sleep;

public class RunnableThreadDemo {

    public static void main(String ...args) {
        var thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                sleep(Duration.ofSeconds(1));
            }
        });
        printThread(thread);
        thread.start();
        printThread(thread);

        thread.interrupt();
    }

}
