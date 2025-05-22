package components;

public class Accessory implements Detail {
    private final int id;
    private final int supplierID;

    public Accessory(int id, int supplierID) {
        this.id = id;
        this.supplierID = supplierID;
    }

    public int getSupplierID() {
        return supplierID;
    }

    @Override
    public int getID() {
        return id;
    }
}
