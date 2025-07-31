package calculator.commands;

import calculator.ExecutionContext;

public class PrintCommand implements Command {
    @Override
    public void execute(ExecutionContext context, String[] args) throws IllegalArgumentException {
        if (args.length != 1) throw new IllegalArgumentException("Для PRINT не требуются аргументы :(");

        System.out.println(context.peek());
    }
}