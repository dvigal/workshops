package practicum.concurrency.synchronizator;

import java.time.Duration;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;

import static practicum.concurrency.Utils.sleep;

public class CyclicBarrierDemo {
    private static final AtomicLong holder = new AtomicLong();

    private static void doWork() {
        for (int a = 0; a < 10000; a++) {
            for (int b = 0; b < 10000; b++) {
                holder.set(a * b);
            }
        }
        System.out.println(String.format("%s competed work cycle", Thread.currentThread().getName()));
    }

    public static void main(String ...args){
        var totalCycles = 5;
        var totalWorkers = 10;
        var latch = new CountDownLatch(totalWorkers);
        var workers = new HashSet<Thread>();
        var cyclicBarrier = new CyclicBarrier(5, () -> System.out.println(String.format("%s %s - all workers started work cycle", Thread.currentThread().getName(), LocalTime.now())));

        var printer = new Thread(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                var waitedCount = workers.stream().filter(worker -> worker.getState() == Thread.State.WAITING).count();
                var activeCount = workers.stream().filter(worker -> worker.getState() == Thread.State.RUNNABLE).count();
                System.out.println(String.format("%s - %s, %s - %s", Thread.State.WAITING, waitedCount, Thread.State.RUNNABLE, activeCount));
                sleep(Duration.ofSeconds(2));
            }
        }, "Worker");
        printer.setDaemon(true);
        printer.start();

        for (int n = 1; n <= totalWorkers; n++) {
            var t = new Thread(() -> {
                latch.countDown();
                for (int cycle = 1; cycle <= totalCycles; cycle++) {
                    try {
                        doWork();
                        cyclicBarrier.await();
                        // pay salary :)
                        System.out.println(String.format("%s - Give me my money!", Thread.currentThread().getName()));
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            }, "worker-" + n);
            workers.add(t);
            t.start();
        }
    }

}
