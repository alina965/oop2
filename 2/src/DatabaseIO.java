import java.io.*;
import java.util.Map;

public class DatabaseIO {
    private final Database database;
    private final String name = "my-database.db";

    public DatabaseIO(Database database) {
        this.database = database;
    }

    public void saveToFile() throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(name))) {
            oos.writeObject(database.getTables());
        }
    }

    public void loadFromFile() throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(name))) {
            database.setTables((Map<String, Table>) ois.readObject());
        }
    }
}
