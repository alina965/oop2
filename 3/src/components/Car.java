package components;

public class Car {
    private final Engine engine;
    private final Body body;
    private final Accessory accessory;
    private final int id;

    public Car(Engine engine, Body body, Accessory accessory, int id) {
        this.accessory = accessory;
        this.body = body;
        this.engine = engine;
        this.id = id;
    }

    @Override
    public String toString() {
        return "Car{id=" + id +
                ", bodyID=" + body.getID() +
                ", engineID=" + engine.getID() +
                ", accessoryID=" + accessory.getID() + "}";
    }
}
