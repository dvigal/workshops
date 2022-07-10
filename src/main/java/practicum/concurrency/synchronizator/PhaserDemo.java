import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Phaser;

/**
 * немного странный пример, но все же
 * у нас есть несколько работников которые собирают заказ магазина для клиента
 * если товара нет на складе, работник прекращает свою деятельность.
 * специально не учитываю количество на складе, стоимость и так далее, хотя их тоже можно раскидать по фазам.
 */
public class PhaserDemo {

    public static void main(String[] args) throws InterruptedException {

        Phaser phaser = new Phaser(); //по умолчанию 0


        String milk = "МОЛОКО";
        String bread = "ХЛЕБ";
        List<String> depot = new ArrayList<>(Arrays.asList(milk,bread)); //склад продуктов

        String clientPetrovich = "Petrovich";


        System.out.println("Запуск потоков");

        //работник идет искать хлеб
        Worker worker = new Worker(phaser,depot,clientPetrovich,50,1000,bread);
        //работник идет искать молко
        Worker worker2 = new Worker(phaser,depot,clientPetrovich,30,500,milk);
        /*работник идет искать конфеты, но так как он их не найдет заказ собран не будет
        можно отключить, чтобы собрать заказ)
        */
        Worker worker3 = new Worker(phaser,depot,clientPetrovich,130,1500,"конфеты");

        new Thread(worker, "Worker1").start();
        new Thread(worker2, "Worker2").start();
        new Thread(worker3, "Worker3").start();

        phaser.arriveAndDeregister();


        System.out.println("Работа программы завершена");


    }
}

class Worker implements  Runnable {

    private final static int DELIVERY_COST = 5; //5 рублей километр.
    private final Phaser phaser;
    private final List<String> depot;
    private final String clientName;
    private final double productCost;
    private final int distance;
    private final String product;


    Worker(Phaser phaser, List<String> depot, String clientName, double productCost, int distance, String product) {
        this.phaser = phaser;
        this.depot = depot;
        this.clientName = clientName;
        this.productCost = productCost;
        this.distance = distance;
        this.product = product;
    }

    @Override
    public void run() {
        phaser.register();
        String threadName = "["+Thread.currentThread().getName()+ "]";
        System.out.println("начинаю искать товар на складе " + product );

        try {
            Thread.sleep(5000); // работник ищет на складе товар
            if (depot.contains(product)){
                System.out.println( threadName + "Продукт найден! считаю стоимость доставки, растояние " + distance);
                phaser.arriveAndAwaitAdvance();
            }
            else {
                System.out.println(threadName + "Такого продукта нет " + product);
                phaser.forceTermination();
            }
        }
        catch (InterruptedException exp) {
            System.out.println(threadName + "Пока работник ходил, поток был завершен.");
        }
        if (phaser.getPhase() > 0) {
            System.out.println(threadName + "растояние " + distance);
            try {
                Thread.sleep(1000); //работник считает
                System.out.println(threadName +"уважаемый " + clientName
                        + " стоимость вместе с доставкой: " + (distance * DELIVERY_COST) + productCost);
                phaser.arriveAndAwaitAdvance();
            }
            catch (InterruptedException exp){
                System.out.println(threadName + "Пока считал, поток был завершен.");
            }
            System.out.println(threadName + "Передаю в службу доставки.");
            phaser.arriveAndDeregister();
        }

    }
}
