package Woche6.ExampleArchitecture.benchmark_two.service;
import Woche6.ExampleArchitecture.benchmark_two.util.Logger;
public class Controller {
    public void handleRequest(String action) {
        Logger.log("Handling action: " + action);
    }
}