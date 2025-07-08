package Woche6.ExampleArchitecture.benchmark_one.helper;

import java.time.LocalDateTime;

public class DateHelper {
    public static String now() {
        return LocalDateTime.now().toString();
    }
}