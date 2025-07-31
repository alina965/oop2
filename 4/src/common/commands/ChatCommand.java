package common.commands;

import java.io.Serializable;

public abstract class ChatCommand implements Serializable {
    private final String name;

    public ChatCommand(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
