package data_base;

import java.io.Serializable;
import java.util.*;

public class Table implements Serializable {
    private static final long serialVersionUID = 2L;

    private final LinkedHashMap<String, Column> columns;
    private final List<Map<String, Object>> rows;
    private final HashMap<String, Set<Object>> uniqueValues;

    public Table() {
        columns = new LinkedHashMap<>();
        rows = new ArrayList<>();
        uniqueValues = new HashMap<>();
    }

    public void addColumn(Column column) throws IllegalArgumentException {
        if (columns.containsKey(column.getName())) {
            throw new IllegalArgumentException("Столбец с именем " + column.getName() + " уже существует");
        }

        columns.put(column.getName(), column);

        if (column.isUnique()) {
            uniqueValues.put(column.getName(), new HashSet<>());
        }
    }

    public void removeColumn(String name)  throws IllegalArgumentException {
        if (!columns.containsKey(name)) {
            throw new IllegalArgumentException("Столбца с этим названием нет: " + name);
        }

        if (columns.get(name).isUnique()) {
            uniqueValues.remove(name);
        }

        columns.remove(name);

        for (Map<String, Object> row : rows) {
            if (!row.containsKey(name)) {
                continue;
            }

            row.remove(name);
        }
    }

    public void addRow(Map<String, Object> newRow)  throws IllegalArgumentException {
        for (Column column : columns.values()) {
            Object value = newRow.get(column.getName());

            if ((value == null) && (column.isNotNull())) {
                throw new IllegalArgumentException("Столбец с именем " + column.getName() + " не может иметь нулевых значений");
            }

            if (column.isUnique()) {
                if (value != null && uniqueValues.get(column.getName()).contains(value)) {
                    throw new IllegalArgumentException("Значение '" + value + "' уже существует в столбце '" + column.getName() + "'");
                }
                if (value != null) {
                    uniqueValues.get(column.getName()).add(value);
                }
            }
        }

        rows.add(newRow);
    }

    public void removeRows(List<Map<String, Object>> rowsToRemove) {
        for (Map<String, Object> row : rowsToRemove) {
            for (Column column : columns.values()) {
                if (column.isUnique()) {
                    Object value = row.get(column.getName());
                    if (value != null) {
                        uniqueValues.get(column.getName()).remove(value);
                    }
                }
            }

            rows.remove(row);
        }
    }

    public HashMap<String, Column> getColumns() {
        return columns;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }
}
