package calculator.commands;

import calculator.ExecutionContext;
import java.util.regex.Pattern;

public class DefineCommand implements Command {
    @Override
    public void execute(ExecutionContext context, String[] args) {
        if (args.length != 3) throw new IllegalArgumentException("Для DEFINE требуется ровно 2 аргумента");

        if (!Pattern.matches("-?\\d+(\\.\\d+)?", args[2])) {
            throw new IllegalArgumentException("Второй аргумент должен быть числом");
        }

        context.define(args[1], Double.parseDouble(args[2]));
    }
}