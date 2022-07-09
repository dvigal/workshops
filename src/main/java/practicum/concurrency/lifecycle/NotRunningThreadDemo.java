package practicum.concurrency.lifecycle;

import practicum.concurrency.Utils;

public class NotRunningThreadDemo {

    public static void main(String ...args) {
        var thread = new Thread("MyThread") {
            public void run() {
                Utils.infinitySleep();
            }
        };

        thread.run();

        // jsp
        // jstack PID
    }

}
