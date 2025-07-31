package common.commands;

public class LoginCommand extends ChatCommand {
    private final String name;
    private final String type;

    public LoginCommand(String name, String type) {
        super("login");
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}