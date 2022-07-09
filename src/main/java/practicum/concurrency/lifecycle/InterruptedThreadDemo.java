package practicum.concurrency.lifecycle;

import java.util.Arrays;

import static practicum.concurrency.Utils.printThread;
import static practicum.concurrency.Utils.waitWhileThreadStateIsNotExpectedState;

public class InterruptedThreadDemo {

    public static void main(String ...args) throws InterruptedException {

        var thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {

            }
            for (;;) {
                if (Thread.currentThread().isInterrupted()) {
                    Arrays.asList(Thread.currentThread().getStackTrace()).forEach(System.out::println);
                    return;
                }
            }
        });
        printThread(thread);
        thread.start();

        printThread(thread);
        waitWhileThreadStateIsNotExpectedState(thread, Thread.State.TIMED_WAITING);
        thread.interrupt();

        thread.join();
        printThread(thread);
    }

}
