package practicum.concurrency.lifecycle;

import static practicum.concurrency.Utils.printThread;
import static practicum.concurrency.Utils.waitWhileThreadStateIsNotExpectedState;

public class JoinedThreadDemo {

    public static void main(String ...args) throws InterruptedException {
        var monitor = new Object();
        var mainThread = Thread.currentThread();
        var printer = new Thread(() -> {
            while (mainThread.getState() == Thread.State.RUNNABLE) {

            }
            System.out.println(String.format("%s: main thread state changed", Thread.currentThread()));
            printThread(mainThread);
            synchronized (monitor) {
                monitor.notify();
            }
        });
        printer.setDaemon(true);
        printer.start();

        var thread = new Thread(() -> {
            synchronized (monitor) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        printThread(thread);
        thread.start();

        printThread(thread);
        waitWhileThreadStateIsNotExpectedState(thread, Thread.State.WAITING);
        printThread(thread);

        System.out.println(String.format("Waiting for the %s to complete...", thread));
        thread.join();
    }

}
