package calculator.commands;

import calculator.ExecutionContext;

public class SqrtCommand implements Command {
    @Override
    public void execute(ExecutionContext context, String[] args) {
        if (args.length != 1) throw new IllegalArgumentException("Для SQRT не требуются аргументы :(");

        context.push(Math.sqrt(context.pop()));
    }
}