package calculator.commands;

import calculator.ExecutionContext;

public interface Command {
    void execute(ExecutionContext context, String[] args);
}