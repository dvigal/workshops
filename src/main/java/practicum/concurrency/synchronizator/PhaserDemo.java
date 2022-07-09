package practicum.concurrency.synchronizator;

import java.time.Duration;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicLong;

import static practicum.concurrency.Utils.sleep;

public class PhaserDemo {
    private static final AtomicLong holder = new AtomicLong();
    private static final Phaser phaser = new Phaser();

    private static void doWork() {
        System.out.println("Work is being done by " + Thread.currentThread().getName());
        for (int a = 0; a < 10000; a++) {
            for (int b = 0; b < 5000; b++) {
                holder.set(a * b);
            }
        }
    }

    private static void doHardWork() {
        System.out.println("Hard work is being done by " + Thread.currentThread().getName());
        for (int a = 0; a < 10000; a++) {
            for (int b = 0; b < 30000; b++) {
                holder.set(a * b);
            }
        }
        System.out.printf("%s completed work %n", Thread.currentThread().getName());
    }

    public static void main(String... args) {
        var totalWorkers = 4;
        phaser.register();

        System.out.println("I'm unfortunate. I'm doing hard work today.");
        System.out.println("But you guys, don't have lunch without me. Just smoke and wait until I finish");

        for (int n = 1; n <= totalWorkers; n++) {
            startWork(n);
        }

        System.out.println("Now it's " + (phaser.getPhase() + 1) + " half of the day");
        doHardWork();
        phaser.arriveAndDeregister();
        System.out.printf("%s - We're having lunch together%n", Thread.currentThread().getName());
        sleep(Duration.ofSeconds(3));
        System.out.println("Now it's " + (phaser.getPhase() + 1) + " half of the day");
    }

    private static void startWork(int numberOfWorker) {
        var t = new Thread(() -> {
            phaser.register();
            doWork();
            System.out.printf("%s - I've done my work! Let's wait for the unfortunate dude%n", Thread.currentThread().getName());

            phaser.arriveAndAwaitAdvance();
            System.out.printf("%s - We're having lunch together%n", Thread.currentThread().getName());

        }, "worker-" + numberOfWorker);
        t.start();
    }
}

