package Woche6.ExampleArchitecture.benchmark_two.domain;
public class Invoice {
    private Order order;
    public Invoice(Order order) { this.order = order; }
    public double calculateTotal() { return 100.0; /* dummy */ }
}