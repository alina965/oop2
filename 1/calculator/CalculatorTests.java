package calculator;

import calculator.commands.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class CalculatorTests {
    @Test
    public void testAddition() {
        ExecutionContext context = new ExecutionContext();

        context.push(1);
        context.push(2);

        Command add = new AddCommand();
        add.execute(context, new String[]{"+"});

        assertEquals(context.pop(), 3.0, "1 + 2 должно быть 3");
    }

    @Test
    public void testSubtraction() {
        ExecutionContext context = new ExecutionContext();

        context.push(9);
        context.push(1);

        Command sub = new SubCommand();
        sub.execute(context, new String[]{"-"});

        assertEquals(context.pop(), 8.0, "9 - 1 должно быть 8");
    }

    @Test
    public void testDivision() {
        ExecutionContext context = new ExecutionContext();

        context.push(8);
        context.push(4);

        Command div = new DivCommand();
        div.execute(context, new String[]{"/"});

        assertEquals(context.pop(), 2.0, "8 / 4 должно быть 2");
    }

    @Test
    public void testDivisionByZero() {
        ExecutionContext context = new ExecutionContext();

        context.push(8);
        context.push(0);

        Command div = new DivCommand();

        try {
            div.execute(context, new String[]{"/"});
            Assert.fail("Ожидалось исключение ArithmeticException, но оно не было выброшено.");
        }
        catch (ArithmeticException exception) {
            assertEquals(exception.getMessage(), "Делить на 0 нельзя.");
        }
    }

    @Test
    public void testMultiplication() {
        ExecutionContext context = new ExecutionContext();

        context.push(9);
        context.push(9);

        Command mul = new MulCommand();
        mul.execute(context, new String[]{"*"});

        assertEquals(context.pop(), 81.0, "9 * 9 должно быть 81");
    }

    @Test
    public void testSQRT() {
        ExecutionContext context = new ExecutionContext();

        context.push(9);

        Command sqrt = new SqrtCommand();
        sqrt.execute(context, new String[]{"SQRT"});

        assertEquals(context.pop(), 3.0, "sqrt(9) должно быть 3");
    }

    @Test
    public void testUnknownCommand() {
        Factory factory = new Factory();
        try {
            factory.getCommand("UNKNOWN_COMMAND");
            Assert.fail("Ожидалось исключение IllegalArgumentException, но оно не было выброшено.");
        }
        catch (IllegalArgumentException exception) {
            assertEquals(exception.getMessage(), "Неизвестная команда: UNKNOWN_COMMAND");
        }
    }

    @Test
    public void testUseUndefinedVariable() {
        ExecutionContext context = new ExecutionContext();
        Command push = new PushCommand();

        try {
            push.execute(context, new String[]{"PUSH", "Y"});
            Assert.fail("Ожидалось исключение IllegalArgumentException, но оно не было выброшено.");
        }
        catch (IllegalArgumentException exception) {
            assertEquals(exception.getMessage(), "Неизвестная переменная - Y");
        }
    }

    @Test
    public void testPopWithEmptyStack() {
        ExecutionContext context = new ExecutionContext();

        try {
            context.pop();
            Assert.fail("Ожидалось исключение IllegalStateException, но оно не было выброшено.");
        }
        catch (IllegalStateException exception) {
            assertEquals(exception.getMessage(), "Стек пуст :(");
        }
    }

    @Test
    public void testPeekWithEmptyStack() {
        ExecutionContext context = new ExecutionContext();

        try {
            context.peek();
            Assert.fail("Ожидалось исключение IllegalStateException, но оно не было выброшено.");
        }
        catch (IllegalStateException exception) {
            assertEquals(exception.getMessage(), "Стек пуст :(");
        }
    }
}
