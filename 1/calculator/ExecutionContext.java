package calculator;

import java.util.Stack;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

public class ExecutionContext {
    private final Stack<Double> stack = new Stack<>();
    private final Map<String, Double> variables = new HashMap<>();
    private static final Logger logger = Logger.getLogger(ExecutionContext.class.getName());

    public void push(double number) {
        stack.push(number);
    }

    public double pop() {
        if (stack.isEmpty()) {
            logger.severe("Попытка вызвать команду POP, хотя стек пуст.");
            System.exit(1);
        }
        return stack.pop();
    }

    public double peek() {
        if (stack.isEmpty()) {
            logger.severe("Попытка вызвать команду PEEK, хотя стек пуст.");
            System.exit(1);
        }
        return stack.peek();
    }

    public void define(String name, double value) {
        variables.put(name, value);
    }

    public double getVariable(String name) {
        if (!variables.containsKey(name)) {
            logger.severe("Попытка использовать неизвестную переменную: " + name);
            System.exit(1);
        }
        return variables.get(name);
    }
}