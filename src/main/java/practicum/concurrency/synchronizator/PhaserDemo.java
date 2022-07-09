package practicum.concurrency.synchronizator;

import java.util.concurrent.Phaser;

public class PhaserDemo {
    Phaser phaser = new Phaser(1);

    public void start() {
        new Thread(new Programmer("Architect", this)).start();
        phaser.arriveAndAwaitAdvance();
        new Thread(new Programmer("Senior Java Developer", this)).start();
        new Thread(new Programmer("Middle Java Developer", this)).start();
        new Thread(new Programmer("Junior Java Developer", this)).start();
        phaser.arriveAndAwaitAdvance();
        new Thread(new Programmer("Tester", this)).start();
        phaser.arriveAndAwaitAdvance();
    }

    class Programmer implements Runnable {
        private String name;
        private PhaserDemo project;

        public Programmer(String name, PhaserDemo project) {
            this.name = name;
            this.project = project;
            this.project.phaser.register();
        }

        @Override
        public void run() {
            this.project.phaser.arriveAndAwaitAdvance();
            System.out.println(this.name + " arrived");
            doWork();
            this.project.phaser.arriveAndDeregister();
            System.out.println(this.name + " deregistered");
        }

        private void doWork() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("Phase " + this.project.phaser.getPhase() + ": " + this.name + " works on project");
            }
        }
    }

    public static void main(String[] args) {
        PhaserDemo phaserDemo = new PhaserDemo();
        phaserDemo.start();
    }
}
