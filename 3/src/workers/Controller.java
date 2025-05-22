package workers;

import components.Accessory;
import components.Body;
import components.Car;
import components.Engine;
import other.IDGenerator;
import storage.Storage;

public class Controller extends Thread {
    private final Storage<Car> carStorage;
    private final ThreadPool threadPool;
    private final Storage<Body> bodyStorage;
    private final Storage<Engine> engineStorage;
    private final Storage<Accessory> accessoryStorage;
    private final IDGenerator carID;
    private volatile boolean isRunning = true;
    private static final int DELAY = 200;

    public Controller(Storage<Car> carStorage, ThreadPool threadPool, Storage<Body> bodyStorage, Storage<Engine> engineStorage, Storage<Accessory> accessoryStorage, IDGenerator carID) {
        this.carStorage = carStorage;
        this.threadPool = threadPool;
        this.bodyStorage = bodyStorage;
        this.engineStorage = engineStorage;
        this.accessoryStorage = accessoryStorage;
        this.carID = carID;
    }

    public void stopWorking() {
        isRunning = false;

        synchronized (carStorage) {
            carStorage.notifyAll();
        }

        this.interrupt();
    }

    @Override
    public void run() {
        try {
            while (isRunning) {
                synchronized (carStorage) {
                    while (carStorage.isFull() && isRunning) {
                        carStorage.wait();
                    }
                }

                threadPool.submit(this::assembleCar);
                System.out.println("[Controller] Отправил задачу на сборку");

                Thread.sleep(DELAY);
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void assembleCar() {
        try {
            Body body = bodyStorage.takeComponent();
            Engine engine = engineStorage.takeComponent();
            Accessory accessory = accessoryStorage.takeComponent();

            Car car = new Car(engine, body, accessory, carID.giveID());
            System.out.println("Worker: собрана машина " + car);
            carStorage.putComponent(car);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}