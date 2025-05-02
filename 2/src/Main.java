public class Main {
    public static void main(String[] args) {
        if (args.length < 3) {
            Database database = new Database();
            DatabaseIO databaseIO = new DatabaseIO(database);

            try {
                databaseIO.loadFromFile();
            }
            catch (Exception e) {
                System.out.println("Создание новой базы данных");
            }

            new DatabaseGUI(database, databaseIO);
        }
        else {
            String dbFile = args[1];
            String command = args[3];

            Database database = new Database();
            database.setDbName(dbFile);

            DatabaseIO databaseIO = new DatabaseIO(database);

            try {
                databaseIO.loadFromFile();
            }
            catch (Exception e) {
                System.out.println("Создание новой базы данных: " + dbFile);
            }

            SQLParser parser = new SQLParser(database);
            try {
                parser.parse(command);
                databaseIO.saveToFile();
                System.out.println("Команда успешно выполнена");
            }
            catch (Exception e) {
                System.err.println("Ошибка: " + e.getMessage());
            }
        }
    }
}