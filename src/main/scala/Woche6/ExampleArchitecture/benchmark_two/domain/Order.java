package Woche6.ExampleArchitecture.benchmark_two.domain;
public class Order {
    private User user;
    private Product product;
    public Order(User user, Product product) {
        this.user = user;
        this.product = product;
    }
    public User getUser() { return user; }
    public Product getProduct() { return product; }
}