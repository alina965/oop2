import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLParser {
    private final Database database;

    public SQLParser(Database database) {
        this.database = database;
    }

    public void parse(String line) throws IllegalArgumentException {
        String normalized = line.trim();

        if (normalized.startsWith("CREATE TABLE")) {
            createTable(line);
        }
        else if (normalized.startsWith("INSERT INTO")) {
            insertInto(line);
        }
        else if (normalized.startsWith("DROP TABLE")) {
            dropTable(line);
        }
        else if (normalized.startsWith("DELETE FROM")) {
            deleteFrom(line);
        }
        else {
            throw new IllegalArgumentException("Недопустимая команда: " + line);
        }
    }

    public void createTable(String line) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile("CREATE TABLE (\\w+) \\((.+)\\)", Pattern.DOTALL); // первая группа захватывает имя таблицы а вторая столбцы

        Matcher matcher = pattern.matcher(line);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Неправильное использование команды CREATE TABLE");
        }

        String tableName = matcher.group(1);
        String columnsDef = matcher.group(2).trim();

        database.addTable(tableName);

        String[] columnDefs = columnsDef.split(";");
        for (String row : columnDefs) {
            row = row.trim();

            String[] parts = row.split(" ");

            if (parts.length < 2) {
                throw new IllegalArgumentException("Неправильная запись аргументов для CREATE TABLE");
            }

            boolean isUnique = Arrays.asList(parts).contains("unique");
            boolean isNotNull = Arrays.asList(parts).contains("not-null");
            database.insertColumn(tableName, parts[0], parts[1], isUnique, isNotNull);
        }
    }

    public void insertInto(String line) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile("INSERT INTO (\\w+) \\((.+)\\)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(line);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Неправильное использование команды INSERT INTO");
        }

        String tableName = matcher.group(1);
        String valuesPart = matcher.group(2).trim();

        if (valuesPart.startsWith("(") && valuesPart.endsWith(")")) {
            valuesPart = valuesPart.substring(1, valuesPart.length() - 1); // убираем скобки
        }

        String[] rows = valuesPart.split("\\),\\s*\\("); // разделитель: "), ("

        List<Map<String, Object>> rowList = new ArrayList<>();

        for (String rowStr : rows) {
            rowStr = rowStr.replaceAll("[()]", "").trim(); // удаляет скобки
            String[] values = rowStr.split(",\\s*"); // в качестве разделителя запятая и пробелы за этой запятой
            Map<String, Object> row = new HashMap<>();

            if (values.length != database.getColumns(tableName).size()) {
                throw new IllegalArgumentException("Число значений не соответствует числу столбцов");
            }

            int i = 0;
            for (Column column : database.getColumns(tableName)) {
                String valueStr = values[i++].trim();
                Object value = parseValue(valueStr, column.getType());
                row.put(column.getName(), value);
            }

            rowList.add(row);
        }

        database.insertRows(tableName, rowList);
    }

    public void dropTable(String line) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile("DROP TABLE (\\w+)");
        Matcher matcher = pattern.matcher(line);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Неправильное использование команды DROP TABLE");
        }

        database.dropTable(matcher.group(1));
    }

    public void deleteFrom(String line) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile("DELETE FROM (\\w+)(?: WHERE (.+))?");
        Matcher matcher = pattern.matcher(line);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Неправильное использование команды DELETE");
        }

        String tableName = matcher.group(1);
        String conditionStr = matcher.group(2);

        if (conditionStr == null || conditionStr.trim().isEmpty()) {
            database.getRows(tableName).clear();
        }

        else {
            String[] conditions = conditionStr.split(" AND ");
            List<Map<String, Object>> rowsToRemove = new ArrayList<>();

            for (Map<String, Object> row : database.getRows(tableName)) {
                boolean match = true;
                for (String cond : conditions) {
                    String[] parts = cond.trim().split("=");
                    String column = parts[0].trim();
                    String value = parts[1].trim().replaceAll("^\"|\"$", ""); // убирает кавычки в начале и в конце строки

                    if (!value.equals(row.get(column).toString())) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    rowsToRemove.add(row);
                }
            }

            database.deleteRows(tableName, rowsToRemove);
        }
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
