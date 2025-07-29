package Woche6.ExampleArchitecture.benchmark_one.shared;

public class SharedUtil {
    public static String format(String template, Object... args) {
        return String.format(template, args);
    }
}