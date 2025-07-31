package data_base.commands;

import data_base.Database;

public class DropCommand implements Command {
    private final String tableName;

    public DropCommand(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public void execute(Database database) {
        database.dropTable(tableName);
    }
}
