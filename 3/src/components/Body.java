package components;

public class Body implements Detail {
    private final int id;

    public Body(int id) {
        this.id = id;
    }

    @Override
    public int getID() {
        return id;
    }
}
