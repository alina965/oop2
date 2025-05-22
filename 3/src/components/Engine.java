package components;

public class Engine implements Detail {
    private final int id;

    public Engine(int id) { this.id = id; }

    @Override
    public int getID() { return id; }
}
