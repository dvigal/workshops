package practicum.concurrency.synchronizator;

import java.util.concurrent.Exchanger;

public class ExchangerDemo {

    public void start() {
        Exchanger<String> exchanger = new Exchanger<>();
        new Thread(new Player("Alex", "Last of Us 2", exchanger)).start();
        new Thread(new Player("Anya", "Cyberpunk 2077", exchanger)).start();

    }

    class Player implements Runnable {
        private String name;
        private String game;
        private Exchanger<String> exchanger;

        public Player(String name, String game, Exchanger<String> exchanger) {
            this.name = name;
            this.game = game;
            this.exchanger = exchanger;
        }

        @Override
        public void run() {
            try {
                System.out.println("Player " + this.name + " plays " + this.game);
                Thread.sleep(3000);
                System.out.println("Player " + this.name + " finished playing " + this.game);
                this.game = this.exchanger.exchange(this.game);
                System.out.println("Player " + this.name + " plays " + this.game);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        ExchangerDemo demo = new ExchangerDemo();
        demo.start();
    }

}
