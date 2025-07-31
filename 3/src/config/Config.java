package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private final Properties props = new Properties();

    public Config(String filename) throws IOException {
        try (FileInputStream fis = new FileInputStream(filename)) {
            props.load(fis);
        }
    }

    public int getProperty(String key) { return Integer.parseInt(props.getProperty(key)); }
}
