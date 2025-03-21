package calculator.commands;

import calculator.ExecutionContext;
import java.util.regex.Pattern;

public class PushCommand implements Command {
    @Override
    public void execute(ExecutionContext context, String[] args) {
        if (args.length != 2) throw new IllegalArgumentException("Нет аргумента для PUSH :(");

        String argument = args[1];
        if (Pattern.matches("-?\\d+(\\.\\d+)?", argument)) {
            context.push(Double.parseDouble(argument));
        }
        else {
            context.push(context.getVariable(argument));
        }
    }
}