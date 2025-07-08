package Woche6.ExampleArchitecture.benchmark_two.util;
import java.util.Properties;
public class ConfigLoader {
    private Properties props = new Properties();
    public ConfigLoader() { /* load defaults */ }
    public String get(String key) { return props.getProperty(key, ""); }
}