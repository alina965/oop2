package calculator;

import calculator.commands.Command;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class Factory {
    private final Map<String, Command> commands = new HashMap<>();
    private static final Logger logger = Logger.getLogger(Factory.class.getName());

    public Factory() {
        loadCommands();
    }

    private void loadCommands() {
        Properties properties = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("commands.properties")) {
            if (input == null) {
                logger.severe("Не удалось найти файл commands.properties");
                throw new RuntimeException("Файл commands.properties не найден");
            }

            properties.load(input);

            for (String key : properties.stringPropertyNames()) {
                String commandName = properties.getProperty(key);

                try {
                    Class<?> commandClass = Class.forName(commandName);
                    Command command = (Command) commandClass.getDeclaredConstructor().newInstance();
                    commands.put(key, command);
                }
                catch (Exception e) {
                    logger.warning("Не удалось создать команду: " + commandName);
                    throw new RuntimeException("Ошибка при создании команды: " + commandName, e);
                }
            }

            logger.info("commands.properties успешно загружен :)");
            }
        catch (IOException e) {
            logger.severe("Произошла ошибка при загрузке commands.properties");
            throw new RuntimeException("Не получилось загрузить файл commands.properties :(");
        }
    }

    public Command getCommand(String name) {
        Command command = commands.get(name);
        if (command == null) {
            logger.warning("Команда " + name + " неизвестна.");
            throw new IllegalArgumentException("Неизвестная команда: " + name);
        }

        logger.info("Была вызвана команда " + name);
        return command;
    }
}