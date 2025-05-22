package suppliers;

import components.Accessory;
import other.IDGenerator;
import storage.Storage;

public class AccessorySupplier extends Thread {
    private final Storage<Accessory> accessoryStorage;
    private final IDGenerator idGenerator;
    private final int frequency;
    private final int id;
    private volatile boolean isRunning = true;

    public AccessorySupplier(int frequency, Storage<Accessory> storage, IDGenerator generator, int id) {
        accessoryStorage = storage;
        idGenerator = generator;
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
                accessoryStorage.putComponent(new Accessory(idGenerator.giveID(), id));
                sleep(frequency);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
