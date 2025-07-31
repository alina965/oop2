package data_base;

import java.io.Serializable;

public class Column implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String type;
    private final boolean unique;
    private final boolean notNull;

    public Column(String name, String type, boolean unique, boolean notNull) {
        this.name = name;
        this.type = type;
        this.unique = unique;
        this.notNull = notNull;
    }

    public String getName() { return name; }

    public String getType() { return type; }

    public boolean isUnique() { return unique; }

    public boolean isNotNull() { return notNull; }
}
