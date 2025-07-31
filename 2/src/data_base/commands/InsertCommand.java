package data_base.commands;

import data_base.Column;
import data_base.Database;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsertCommand implements Command {
    private final String tableName;
    private final List<String[]> rawRows;

    public InsertCommand(String tableName, List<String[]> rawRows) {
        this.tableName = tableName;
        this.rawRows = rawRows;
    }

    @Override
    public void execute(Database database) {
        List<Column> columns = database.getColumns(tableName);
        List<Map<String, Object>> rows = parseRows(columns);
        database.insertRows(tableName, rows);
    }

    private List<Map<String, Object>> parseRows(List<Column> columns) {
        List<Map<String, Object>> rows = new ArrayList<>();

        for (String[] rawValues : rawRows) {
            if (rawValues.length != columns.size()) {
                throw new IllegalArgumentException("Количество значений не совпадает с количеством столбцов");
            }

            Map<String, Object> row = new HashMap<>();

            for (int i = 0; i < columns.size(); i++) {
                Column column = columns.get(i);
                String valueStr = rawValues[i].trim();
                Object value = parseValue(valueStr, column.getType());
                row.put(column.getName(), value);
            }
            rows.add(row);
        }

        return rows;
    }

    private Object parseValue(String valueStr, String type) throws IllegalArgumentException {
        if ("NULL".equalsIgnoreCase(valueStr)) return null;

        switch (type.toUpperCase()) {
            case "INT": return Integer.parseInt(valueStr);
            case "STRING": return valueStr.replaceAll("^\"|\"$", "");
            case "BOOLEAN": return Boolean.parseBoolean(valueStr);
            case "DATE":
                try {
                    valueStr = valueStr.replaceAll("^\"|\"$", "");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
                    return ZonedDateTime.parse(valueStr, formatter).toLocalDateTime();
                }
                catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Неверный формат даты. Пример: 2025-02-19T21:43:15+0000");
                }
            default: throw new IllegalArgumentException("Неподдерживаемый тип: " + type);
        }
    }
}
