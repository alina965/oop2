package calculator.commands;

import calculator.ExecutionContext;

public class SubCommand implements Command {
    @Override
    public void execute(ExecutionContext context, String[] args) {
        if (args.length != 1) throw new IllegalArgumentException("Для \"-\" не требуются аргументы :(");

        double num = context.pop();
        context.push(context.pop() - num);
    }
}