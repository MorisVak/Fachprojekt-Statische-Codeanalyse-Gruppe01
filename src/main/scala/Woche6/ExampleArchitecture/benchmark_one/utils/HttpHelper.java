package Woche6.ExampleArchitecture.benchmark_one.utils;

public class HttpHelper {
    public static String get(String url) {
        // Dummy GET
        return "GET response from " + url;
    }

    public static String post(String url, String body) {
        // Dummy POST
        return "POST to " + url + ": " + body;
    }
}