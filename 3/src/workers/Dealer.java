package workers;

import components.Car;
import storage.Storage;

public class Dealer extends Thread {
    private final int frequency;
    private final Storage<Car> storage;
    private volatile boolean isRunning = true;

    public Dealer(int frequency, Storage<Car> storage) {
        this.frequency = frequency;
        this.storage = storage;
    }

    public void stopWorking() {
        isRunning = false;
        this.interrupt();
    }

    @Override
    public void run() {
        try {
            while (isRunning) {
                Car car = storage.takeComponent();
                System.out.println("Дилер запросил машину: " + car);
                sleep(frequency);
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
