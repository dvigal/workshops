package practicum.concurrency.synchronizator;

import java.time.Duration;
import java.util.concurrent.Exchanger;
import java.util.concurrent.atomic.AtomicLong;

import static practicum.concurrency.Utils.printThread;
import static practicum.concurrency.Utils.sleep;

public class ExchangerDemo {
    private static final AtomicLong holder = new AtomicLong();

    public static void main(String[] args) {
        Exchanger<String> exchanger = new Exchanger<>();
        new Thread(() -> {
            try {
                doNothing();
                Thread thread = Thread.currentThread();
                String workDone = thread.getName();
                printState(thread, workDone, true);
                System.out.println("Waiting for my industrious friend..");
                workDone = exchanger.exchange(thread.getName());
                printState(thread, workDone, false);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "Ready to sleep").start();

        new Thread(() -> {
            try {
                doLongTimeWork();
                Thread thread = Thread.currentThread();
                String workDone = thread.getName();
                printState(thread, workDone, true);
                workDone = exchanger.exchange(thread.getName());
                sleep(Duration.ofMillis(100));
                printState(thread, workDone, false);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "Ready to work").start();
    }

    private static void printState(Thread thread, String workDone, boolean beforeExchanging) {
        System.out.println("------------------------------------------------------------------");
        printThread(Thread.currentThread());
        System.out.println( beforeExchanging ?
                "Before exchanging. Work done: " + workDone :
                "After exchanging. Work done: " + workDone);
    }

    private static void doLongTimeWork() {
        for (int a = 0; a < 10000; a++) {
            for (int b = 0; b < 50000; b++) {
                holder.set(a * b);
            }
        }
        Thread.currentThread().setName("I'm aching everywhere. But give me more job");
    }

    private static void doNothing() {
        sleep(Duration.ofNanos(1));
        Thread.currentThread().setName("Nothing was done. I want to sleep more");
    }
}
