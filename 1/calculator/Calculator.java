package calculator;

import calculator.commands.Command;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public class Calculator {
    public static ExecutionContext context = new ExecutionContext();
    public static Factory factory = new Factory();
    private static final Logger logger = Logger.getLogger(Calculator.class.getName());

    public static void main(String[] args) {
        logger.info("Калькулятор запущен.");

        if (args.length > 0) {
            logger.info("Чтение команд из файла.");

            try (BufferedReader reader = new BufferedReader(new FileReader(args[0]))) {
                executeCommands(reader);
            }
            catch (IOException e) {
                logger.severe("Произошла ошибка при чтении из файла: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }

        else {
            logger.info("Ожидание ввода команд с клавиатуры.");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                executeCommands(reader);
            }
            catch (IOException e) {
                logger.severe("Произошла ошибка ввода: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }


    }

    private static void executeCommands(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }

            String[] parts = line.split(" ");

            Command command = factory.getCommand(parts[0]);
            command.execute(context, parts);
        }
    }
}