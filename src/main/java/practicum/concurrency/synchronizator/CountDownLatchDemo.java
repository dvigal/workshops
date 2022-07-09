package practicum.concurrency.synchronizator;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import static practicum.concurrency.Utils.printThread;

public class CountDownLatchDemo {
    private static final AtomicLong holder = new AtomicLong();

    private static void doWork() {
        printThread(Thread.currentThread());
        for (int a = 0; a < 10_000; a++) {
            for (int b = 0; b < 10_000; b++) {
                holder.set(a * b);
            }
        }
    }

    public static void main(String ...args) throws InterruptedException {
        var totalWorkers = 10;
        var latch = new CountDownLatch(totalWorkers);

        var printer = new Thread(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "Worker");
        printer.setDaemon(true);
        printer.start();

        for (int n = 1; n <= totalWorkers; n++) {
            var t = new Thread(() -> {
                doWork();
                System.out.println(String.format("%s completed job", Thread.currentThread().getName()));
                latch.countDown();
            }, "worker-" + n);
            t.start();
        }

        latch.await();

        System.out.println("All workers completed jobs");
        // pay salary :)
    }

}
