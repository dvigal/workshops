import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;

/*
работа склада, продавец формирует заказ и передает складу заказ
после чего склад готовит к выдаче, после чего возвращает его обратно.
продавец сообщает информацию клиенту
 */
public class ExchangerDemo {
    public static void main(String[] args) throws InterruptedException {
        Order order = new Order();
        String product = "БАТОН";
        Exchanger<Order> exchanger = new Exchanger<>();
        Salesman salesman = new Salesman(order, product,exchanger);
        StoreClerk storeClerk = new StoreClerk(exchanger,order);
        System.out.println("старт потоков");
        List<Thread> threads =  new ArrayList<>();
        threads.add(new Thread(salesman,"SalesMan"));
        threads.add( new Thread(storeClerk,"StoreClerk"));

        for (Thread thread: threads
             ) {
            thread.start();
        }

        for (Thread thread : threads
                ) {
            thread.join();
        }
        System.out.println("работа завершена");


    }
}

class Order {
    private String productName;
    private boolean isReady;

    public String getProductName() {
        return productName;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    Order() {
        isReady = false;
    }

    @Override
    public String toString() {
        return "Order{" +
                "productName='" + productName + '\'' +
                '}';
    }
}

class Salesman implements Runnable {
    private final Order order;
    private final String productName;
    private final Exchanger<Order> exchanger;

    Salesman(Order order, String productName, Exchanger<Order> exchanger) {
        this.order = order;
        this.productName = productName;
        this.exchanger = exchanger;
    }

    @Override
    public void run() {
        System.out.println("Готовим заказ");
        try {
            Thread.sleep(1000); //работник формирует заказ
            order.setProductName(productName); //заполняет заказ
        } catch (InterruptedException e) {
            System.out.println("когда готовил, поток завершился, не успел(");
        }
        //сюда будет возвращено управление когда склад обработает заказ
        try {
            Order orderFromStore = exchanger.exchange(order);
            if (orderFromStore.isReady()) { //если со склада прилетел заполненный - все ок
                System.out.println("Уважаемый клиент, ваш заказ готов к выдаче: " + order);
            }
            else {
                System.out.println("Заказ сформировать не удалось...извините..");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
}

class StoreClerk implements  Runnable {
    private final Exchanger<Order> exchanger;

    private final Order order;


    StoreClerk(Exchanger<Order> exchanger, Order order) {
        this.exchanger = exchanger;
        this.order = order;
    }

    @Override
    public void run() {
        try {
            System.out.println("Проверяю наличие на складе " + order );
            order.setReady(true);
            exchanger.exchange(order);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
