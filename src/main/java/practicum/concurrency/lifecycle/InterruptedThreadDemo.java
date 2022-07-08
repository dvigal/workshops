package practicum.concurrency.lifecycle;

import practicum.concurrency.Utils;

import static practicum.concurrency.Utils.*;

public class InterruptedThreadDemo {

    public static void main(String ...args) throws InterruptedException {

        var thread = new Thread(Utils::infinitySleep);
        printThread(thread);
        thread.start();

        printThread(thread);
        waitWhileThreadStateIsNotExpectedState(thread, Thread.State.TIMED_WAITING);
        thread.interrupt();

        thread.join();
        printThread(thread);
    }

}
