package que1;

import java.util.concurrent.Semaphore;


public class Main {
    private static final int BakeryLimit = 37; // to stop the program after a time i took this limit for bakery though it is not mentioned in problem statement
    private static final int SHOP_THREADS_COUNT = 4; // i took 4 shop here
    private static final int MAX_INVENTORY = 10; // maximum inventory capacity

    private static int breadProduced = 0;
    private static int breadSold = 0;
    private static int breadInventory = 0; // how many is still availabe

    private static final Semaphore mutex = new Semaphore(1);
    private static final Semaphore inventoryAvailable = new Semaphore(MAX_INVENTORY );
    private static final Semaphore breadAvailable = new Semaphore(0);

    public static void main(String[] args) {
        Thread bakeryThread = new Thread(new Bakery());
        bakeryThread.start();

        for (int i = 1; i < SHOP_THREADS_COUNT; i++) {
            Thread shopThread = new Thread(new Shop());
            shopThread.start();

            try {
                bakeryThread.join();
                shopThread.join();
                bakeryThread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Parent Quitting") ;

    }

    static class Bakery implements Runnable  {
        @Override
        public void run() {
            for(int i=0;i<=BakeryLimit ;i++) {

                try {
                    inventoryAvailable.acquire() ;
                    mutex.acquire(); //accuring lock for the BakeryThread so that none else can access

                    breadProduced++; //making bread
                    breadInventory++; //here im addding those bread in inventory or storage

                    System.out.println("Bakery produced bread. Total produced: " + breadProduced);

                    breadAvailable.release();
                    mutex.release(); //relesing the lock for other thread
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class Shop implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    breadAvailable.acquire();
                    mutex.acquire();  //accuring lock for the shopThread so that none else can access

                    if (breadInventory > 0) {
                        breadInventory--; // removing bread from stroage
                        breadSold++; // counting how many selling 
                        System.out.println("Shop sold bread. Total sold: " + breadSold);
                        mutex.release(); //relesing the lock for other thread
                        inventoryAvailable.release();
                    }
                    else {
                        System.out.println("NO Bread Availabe. Waiting for new bread to be made");
                        mutex.release();  //relesing the lock for other thread
                        inventoryAvailable.release();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                }
        }
    }
}
