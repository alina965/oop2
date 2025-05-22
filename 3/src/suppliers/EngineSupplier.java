package suppliers;

import components.Engine;
import other.IDGenerator;
import storage.Storage;

public class EngineSupplier extends Thread {
    private final Storage<Engine> engineStorage;
    private final IDGenerator idGenerator;
    private final int frequency;
    private volatile boolean isRunning = true;

    public EngineSupplier(int frequency, Storage<Engine> storage, IDGenerator generator) {
        engineStorage = storage;
        idGenerator = generator;
        this.frequency = frequency;
    }

    public void stopWorking() {
        isRunning = false;
        this.interrupt();
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                engineStorage.putComponent(new Engine(idGenerator.giveID()));
                sleep(frequency);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
