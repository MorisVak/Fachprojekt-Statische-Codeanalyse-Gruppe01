package Woche6.ExampleArchitecture.benchmark_one.core;

public abstract class AbstractController {
    protected void log(String msg) {
        System.out.println("[Controller] " + msg);
    }
}