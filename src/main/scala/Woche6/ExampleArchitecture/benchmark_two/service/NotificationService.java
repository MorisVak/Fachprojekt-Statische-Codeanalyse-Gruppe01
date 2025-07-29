package Woche6.ExampleArchitecture.benchmark_two.service;
import Woche6.ExampleArchitecture.benchmark_two.domain.User;
import Woche6.ExampleArchitecture.benchmark_two.util.Logger;
public class NotificationService {
    public void notify(User u, String message) {
        Logger.log("Notify " + u.getName() + ": " + message);
    }
}