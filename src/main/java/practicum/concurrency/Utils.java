package practicum.concurrency;

import java.time.Duration;

public class Utils {

    public static void printThread(Thread thread) {
        System.out.println(
                String.format("Thread[id=%s, name=%s, state=%s, alive=%s, daemon=%s]",
                        thread.getId(),
                        thread.getName(),
                        thread.getState(),
                        thread.isAlive(),
                        thread.isDaemon()
                )
        );
    }

    public static void waitWhileThreadStateIsNotExpectedState(Thread thread, Thread.State expectedState) {
        for (int i = 0; i < 100; i++) {
            if (thread.getState() == expectedState) {
                break;
            }
            sleep(Duration.ofMillis(100));
        }
    }

    public static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            rethrow(e);
        }
    }

    public static void infinitySleep() {
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            rethrow(e);
        }
    }

    private static <E extends Exception> void rethrow(Exception e) throws E {
        throw (E) e;
    }

}
