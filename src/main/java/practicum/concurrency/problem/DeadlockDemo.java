package practicum.concurrency.problem;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

import static practicum.concurrency.Utils.*;

public class DeadlockDemo {

    public static void main(String ...args) throws InterruptedException {
        var latch1 = new CountDownLatch(1);
        var latch2 = new CountDownLatch(1);
        var resource1 = new Object();
        var resource2 = new Object();

        var t1 = new Thread(() -> {
            try {
                synchronized (resource1) {
                    latch1.countDown();
                    latch2.await();
                    synchronized (resource2) {
                        infinitySleep();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        var t2 = new Thread(() -> {
            synchronized (resource2) {
                latch2.countDown();
                try {
                    latch1.await();
                    synchronized (resource1) {
                        infinitySleep();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        var printer = new Thread(() -> {
           for (;;) {
               printThread(t1);
               printThread(t2);
               sleep(Duration.ofSeconds(1));
           }
        });
        printer.setDaemon(true);
        printer.start();

        t1.start();
        t2.start();

        t1.join();
        t2.join();
    }
}
