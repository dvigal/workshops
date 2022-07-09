package practicum.concurrency.synchronizator;

import java.time.Duration;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

import static practicum.concurrency.Utils.sleep;

public class SemaphoreDemo {
    private static final AtomicLong holder = new AtomicLong();

    private static void doWork() {
        for (int a = 0; a < 10_000; a++) {
            for (int b = 0; b < 10_000; b++) {
                holder.set(a * b);
            }
        }
    }

    public static void main(String ...args) {
        final var maxWorkers = 10;
        final var totalWorkers = 100;
        var latch = new CountDownLatch(totalWorkers);
        var semaphore = new Semaphore(maxWorkers);
        var workers = new HashSet<Thread>();

        var printer = new Thread(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
               if (!workers.isEmpty()) {
                   var waitedCount = workers.stream()
                           .filter(worker -> worker.getState() == Thread.State.WAITING)
                           .count();
                   var activeCount = workers.stream().filter(worker -> worker.getState() == Thread.State.RUNNABLE).count();
                   System.out.println(String.format("%s - %s, %s - %s", Thread.State.WAITING, waitedCount, Thread.State.RUNNABLE, activeCount));
               }
               sleep(Duration.ofMillis(500));
           }
        }, "Worker");
        printer.setDaemon(true);
        printer.start();

        for (int n = 1; n <= totalWorkers; n++) {
            var t = new Thread(() -> {
                try {
                    latch.countDown(); // ignore this line of code
                    semaphore.acquire();
                    doWork();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaphore.release();
                    System.out.println(String.format("%s release semaphore", Thread.currentThread().getName()));
                }
            }, "worker-" + n);
            workers.add(t);
            t.start();
        }
    }

}
