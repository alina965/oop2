package data_base.commands;

import data_base.Database;

public interface Command {
    void execute(Database database);
}
