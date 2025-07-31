package calculator.commands;

import calculator.ExecutionContext;

public class AddCommand implements Command {
    @Override
    public void execute(ExecutionContext context, String[] args) throws IllegalArgumentException {
        if (args.length != 1) throw new IllegalArgumentException("для \"+\" не требуются аргументы :(");

        context.push(context.pop() + context.pop());
    }
}