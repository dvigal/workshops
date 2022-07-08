package practicum.concurrency.lifecycle;

import static practicum.concurrency.Utils.printThread;
import static practicum.concurrency.Utils.waitWhileThreadStateIsNotExpectedState;

public class WaitingThreadDemo {

    public static void main(String ...args) {
        var monitor = new Object();
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

        synchronized (monitor) {
            monitor.notify();
        }
    }

}
