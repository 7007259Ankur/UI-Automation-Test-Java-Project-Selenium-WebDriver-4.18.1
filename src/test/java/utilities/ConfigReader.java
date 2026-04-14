package utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigReader loads configuration from config.properties and exposes
 * a static accessor so any class can retrieve properties without
 * re-reading the file on every call.
 */
public class ConfigReader {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ConfigReader.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties not found on classpath");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    /** Private constructor — utility class, no instantiation. */
    private ConfigReader() {}

    /**
     * Returns the value associated with the given key.
     *
     * @param key property key defined in config.properties
     * @return trimmed property value
     * @throws RuntimeException if the key is not found
     */
    public static String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Property '" + key + "' not found in config.properties");
        }
        return value.trim();
    }
}
