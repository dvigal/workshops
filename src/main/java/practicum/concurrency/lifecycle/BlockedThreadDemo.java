package practicum.concurrency.lifecycle;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

import static practicum.concurrency.Utils.*;

public class BlockedThreadDemo {

    public static void main(String ...args) throws InterruptedException {
        var latch = new CountDownLatch(1);
        var monitor = new Object();
        var mainThread = Thread.currentThread();

        var printer = new Thread(() -> {
            for (;;) {
                System.out.println(String.format("%s: Thread %s is blocked", Thread.currentThread(), mainThread));
                sleep(Duration.ofMillis(1000));
            }
        });
        printer.setDaemon(true);
        printer.start();

        var worker = new Thread(() -> {
            synchronized (monitor) {
                System.out.println(String.format("%s: 'Doing long job...'", Thread.currentThread()));
                latch.countDown();
                for (;;);
            }
        });

        worker.start();
        latch.await();

        printThread(mainThread);
        System.out.println(String.format("%s: 'Trying lock monitor...'", mainThread));
        synchronized (monitor) {
        }

    }

}
