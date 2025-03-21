package calculator.commands;

import calculator.ExecutionContext;

public class PopCommand implements Command {
    @Override
    public void execute(ExecutionContext context, String[] args) {
        if (args.length != 1) throw new IllegalArgumentException("Для POP не требуются аргументы :(");

        context.pop();
    }
}