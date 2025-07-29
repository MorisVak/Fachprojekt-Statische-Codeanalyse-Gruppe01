package Woche6.ExampleArchitecture.benchmark_two.service;
import Woche6.ExampleArchitecture.benchmark_two.domain.Order;
import Woche6.ExampleArchitecture.benchmark_two.domain.User;
import Woche6.ExampleArchitecture.benchmark_two.domain.Product;
import Woche6.ExampleArchitecture.benchmark_two.util.Logger;
public class OrderService {
    public Order createOrder(User u, Product p) {
        Logger.log("Creating order for user " + u.getName());
        return new Order(u, p);
    }
}