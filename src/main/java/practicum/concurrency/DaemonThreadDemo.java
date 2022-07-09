package practicum.concurrency;

import static practicum.concurrency.Utils.printThread;

public class DaemonThreadDemo {

    public static void main(String ...args) {
        var daemonThread = new Thread(Utils::infinitySleep);
        daemonThread.setDaemon(true);
        daemonThread.start();
        printThread(daemonThread);
    }

}
