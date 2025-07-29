package Woche6.ExampleArchitecture.benchmark_two.service;
import Woche6.ExampleArchitecture.benchmark_two.domain.User;
import Woche6.ExampleArchitecture.benchmark_two.util.Logger;
public class UserService {
    public User createUser(String name) {
        Logger.log("Creating user " + name);
        return new User(name);
    }
}
