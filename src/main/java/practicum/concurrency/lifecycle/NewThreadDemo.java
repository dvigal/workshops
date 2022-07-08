package practicum.concurrency.lifecycle;

import static practicum.concurrency.Utils.printThread;

public class NewThreadDemo {

    public static void main(String ...args) {
        var thread = new Thread(() -> {});

        printThread(thread);
    }

}
