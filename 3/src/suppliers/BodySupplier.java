package suppliers;

import components.Body;
import other.IDGenerator;
import storage.Storage;

public class BodySupplier extends Thread {
    private final Storage<Body> bodyStorage;
    private final IDGenerator idGenerator = new IDGenerator();
    private final int frequency;
    private volatile boolean isRunning = true;

    public BodySupplier(int frequency, Storage<Body> storage) {
        bodyStorage = storage;
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
                bodyStorage.putComponent(new Body(idGenerator.getID()));
                sleep(frequency);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
