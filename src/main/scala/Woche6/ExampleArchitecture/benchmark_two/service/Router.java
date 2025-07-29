package Woche6.ExampleArchitecture.benchmark_two.service;
public class Router {
    public void route(String path) {
        Controller ctrl = new Controller();
        ctrl.handleRequest(path);
    }
}