import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {

    private final Map<String, Table> tables = new HashMap<>();
    private String dbName;

    public void addTable(String name) throws IllegalArgumentException {
        if (tables.containsKey(name)) {
            throw new IllegalArgumentException("Таблица с таким названием уже есть: " + name);
        }

        Table table = new Table();

        tables.put(name, table);
    }

    public void dropTable(String name) throws IllegalArgumentException {
        if (!tables.containsKey(name)) {
            throw new IllegalArgumentException("Таблицы стаким названием нет: " + name);
        }

        tables.remove(name);
    }

    public void insertColumn(String tableName, String columnName, String type, boolean isUnique, boolean isNotNull) {
        Table table = getTable(tableName);
        Column column = new Column(columnName, type, isUnique, isNotNull);
        table.addColumn(column);
    }

    public void insertRows(String tableName, List<Map<String, Object>> rows) {
        Table table = getTable(tableName);

        for (Map<String, Object> row : rows) {
            table.addRow(row);
        }
    }

    public void deleteRows(String tableName, List<Map<String, Object>> rowsToRemove) {
        getTable(tableName).removeRows(rowsToRemove);
    }

    public Table getTable(String name) throws IllegalArgumentException {
        Table table = tables.get(name);
        if (table == null) {
            throw new IllegalArgumentException("Таблица не найдена: " + name);
        }

        return table;
    }

    public Map<String, Table> getTables() { return tables; }

    public List<Map<String, Object>> getRows(String tableName) { return getTable(tableName).getRows(); }

    public List<Column> getColumns(String tableName) { return new ArrayList<>(getTable(tableName).getColumns().values()); }

    public void setDbName(String name) { dbName = name; }

    public void setTables(Map<String, Table> tables) {
        this.tables.clear();
        this.tables.putAll(tables);
    }
}