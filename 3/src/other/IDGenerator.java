package other;

import java.util.concurrent.atomic.AtomicInteger;

public class IDGenerator {
    private final AtomicInteger currentID = new AtomicInteger(0);

    public int giveID() {
        return currentID.getAndIncrement();
    }
}
