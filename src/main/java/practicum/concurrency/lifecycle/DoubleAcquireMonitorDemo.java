package practicum.concurrency.lifecycle;

public class DoubleAcquireMonitorDemo {

    public static void main(String ...args) throws InterruptedException {
        var monitor = new Object();
        var worker = new Thread(() -> {
            synchronized (monitor) {
                synchronized (monitor) {
                    System.out.println("Doing work...");
                }
            }
        });
        worker.start();

        worker.join();
    }
}
