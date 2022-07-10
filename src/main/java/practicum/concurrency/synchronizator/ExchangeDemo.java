package practicum.concurrency.synchronizator;

import java.util.concurrent.Exchanger;

public class ExchangeDemo {

    public static void main(String[] args) {
        var exchanger = new Exchanger<String>();
        buildThread(exchanger, "seller", "goods");
        buildThread(exchanger, "buyer", "money");
    }

    public static void buildThread(final Exchanger<String> exchanger, final String name, final String messageForExchange) {
        var thread = new Thread(() -> {
            try {
                var receivedMessage = exchanger.exchange(messageForExchange);
                System.out.println(String.format("The %s gives %s and takes %s", name, messageForExchange, receivedMessage));
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }, name);
        thread.start();
    }
}
