package practicum.concurrency.synchronizator;

import java.util.List;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicLong;

public class PhaserDemo {

    private static final AtomicLong holder = new AtomicLong();

    private static void doWork() {
        for (int a = 0; a < 10000; a++) {
            for (int b = 0; b < 10000; b++) {
                holder.set(a * b);
            }
        }
        System.out.println(String.format("%s finished his part of job", Thread.currentThread().getName()));
    }

    private static void doWorkLazy() {
        for (int a = 0; a < 10000; a++) {
            for (int b = 0; b < 50000; b++) {
                holder.set(a * b);
            }
        }
        System.out.println(String.format("%s finally finished his part of job", Thread.currentThread().getName()));
    }

    public static void main(String[] args) {

        var phases = List.of("old walls demolition", "new walls construction", "walls painting");
        var totalWorkers = 3;
        var phaser = new Phaser();

        for (int n = 1; n <= totalWorkers; n++) {
            var thread = new Thread(() -> {
                phaser.register();
                var workerName = Thread.currentThread().getName();

                phases.forEach(phase -> {
                    System.out.println(String.format("Phase #%s: %s, is doing by %s", phaser.getPhase(), phase, workerName));
                    if(workerName.equals("worker-2")) {
                        doWorkLazy();
                    } else {
                        doWork();
                    }
                    phaser.arriveAndAwaitAdvance();
                });
                phaser.arriveAndDeregister();
            }, "worker-" + n);
            thread.start();
        }
    }
}
