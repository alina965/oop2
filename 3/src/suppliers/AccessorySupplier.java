package suppliers;

import components.Accessory;
import other.IDGenerator;
import storage.Storage;

public class AccessorySupplier extends Thread {
    private final Storage<Accessory> accessoryStorage;
    private final IDGenerator idGenerator = new IDGenerator();
    private final int frequency;
    private final int id;
    private volatile boolean isRunning = true;

    public AccessorySupplier(int frequency, Storage<Accessory> storage, int id) {
        accessoryStorage = storage;
        this.frequency = frequency;
        this.id = id;
    }

    public void stopWorking() {
        isRunning = false;
        this.interrupt();
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                accessoryStorage.putComponent(new Accessory(idGenerator.getID(), id));
                sleep(frequency);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
