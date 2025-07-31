package data_base.commands;

import data_base.Column;
import data_base.Database;

import java.util.List;

public class CreateCommand implements Command {
    private final String tableName;
    private final List<Column> columns;

    public CreateCommand(String tableName, List<Column> columns) {
        this.tableName = tableName;
        this.columns = columns;
    }

    @Override
    public void execute(Database database) {
        database.addTable(tableName, columns);
    }
}
