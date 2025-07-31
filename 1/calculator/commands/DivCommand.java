package calculator.commands;

import calculator.ExecutionContext;

public class DivCommand implements Command {
    @Override
    public void execute(ExecutionContext context, String[] args) throws IllegalArgumentException, ArithmeticException{
        if (args.length != 1) throw new IllegalArgumentException("Для \"/\" не требуются аргументы :(");

        double num = context.pop();
        if (num != 0) {
            context.push(context.pop() / num);
        } else {
            throw new ArithmeticException("Делить на 0 нельзя.");
        }
    }
}