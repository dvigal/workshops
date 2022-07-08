package practicum.concurrency.lifecycle;

import static practicum.concurrency.Utils.printThread;

public class TerminatedThreadDemo {

    public static void main(String ...args) throws InterruptedException {
        var thread = new Thread(() -> {
            System.out.println("Doing work");
        });
        printThread(thread);
        thread.start();
        printThread(thread);

        thread.join();
        printThread(thread);
    }

}
