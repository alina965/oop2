package data_base.commands;

import data_base.Column;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLParser {
    public Command parse(String line) throws IllegalArgumentException {
        String normalized = line.trim();

        if (normalized.startsWith("CREATE TABLE")) {
            return parseCreateTable(line);
        }
        else if (normalized.startsWith("INSERT INTO")) {
            return parseInsertInto(line);
        }
        else if (normalized.startsWith("DROP TABLE")) {
            return parseDropTable(line);
        }
        else if (normalized.startsWith("DELETE FROM")) {
            return parseDeleteFrom(line);
        }
        else {
            throw new IllegalArgumentException("Недопустимая команда: " + line);
        }
    }

    private CreateCommand parseCreateTable(String line) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile("CREATE TABLE (\\w+) \\((.+)\\)", Pattern.DOTALL); // первая группа захватывает имя таблицы а вторая столбцы

        Matcher matcher = pattern.matcher(line);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Неправильное использование команды CREATE TABLE");
        }

        String tableName = matcher.group(1);
        String columnsDef = matcher.group(2).trim();

        String[] columnDefs = columnsDef.split(";");

        List<Column> columns = new ArrayList<>();

        for (String row : columnDefs) {
            row = row.trim();

            String[] parts = row.split(" ");

            if (parts.length < 2) {
                throw new IllegalArgumentException("Неправильная запись аргументов для CREATE TABLE");
            }

            boolean isUnique = Arrays.asList(parts).contains("unique");
            boolean isNotNull = Arrays.asList(parts).contains("not-null");
            columns.add(new Column(parts[0], parts[1], isUnique, isNotNull));
        }

        return new CreateCommand(tableName, columns);
    }

    private InsertCommand parseInsertInto(String line) throws IllegalArgumentException {
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
        List<String[]> rawRows = new ArrayList<>();

        for (String rowStr : rows) {
            rowStr = rowStr.replaceAll("[()]", "").trim(); // удаляет скобки
            String[] values = rowStr.split(",\\s*"); // в качестве разделителя запятая и пробелы за этой запятой

            rawRows.add(values);
        }

        return new InsertCommand(tableName, rawRows);
    }

    private DropCommand parseDropTable(String line) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile("DROP TABLE (\\w+)");
        Matcher matcher = pattern.matcher(line);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Неправильное использование команды DROP TABLE");
        }

        return new DropCommand(matcher.group(1));
    }

    private DeleteCommand parseDeleteFrom(String line) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile("DELETE FROM (\\w+)(?: WHERE (.+))?");
        Matcher matcher = pattern.matcher(line);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Неправильное использование команды DELETE");
        }

        String tableName = matcher.group(1);
        String conditionStr = matcher.group(2);

        String[] conditions = conditionStr.split(" AND ");

        return new DeleteCommand(tableName, conditions);
    }
}
