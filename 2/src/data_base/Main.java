package data_base;

import data_base.commands.Command;
import data_base.commands.SQLParser;

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
            String sql = args[3];

            Database database = new Database();
            database.setDbName(dbFile);

            DatabaseIO databaseIO = new DatabaseIO(database);

            try {
                databaseIO.loadFromFile();
            }
            catch (Exception e) {
                System.out.println("Создание новой базы данных: " + dbFile);
            }

            SQLParser parser = new SQLParser();
            try {
                Command command = parser.parse(sql);
                command.execute(database);
                databaseIO.saveToFile();
                System.out.println("Команда успешно выполнена");
            }
            catch (Exception e) {
                System.err.println("Ошибка: " + e.getMessage());
            }
        }
    }
}