package data_base.commands;

import data_base.Database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeleteCommand implements Command {
    private final String tableName;
    private final String[] conditions;

    public DeleteCommand(String tableName, String[] conditions) {
        this.tableName = tableName;
        this.conditions = conditions;
    }

    @Override
    public void execute(Database database) {
        List<Map<String, Object>> rowsToRemove = parseCondition(database);

        database.deleteRows(tableName, rowsToRemove);
    }

    private List<Map<String, Object>> parseCondition(Database database) {
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

        return rowsToRemove;
    }
}
