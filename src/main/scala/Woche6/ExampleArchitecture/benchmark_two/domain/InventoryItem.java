package Woche6.ExampleArchitecture.benchmark_two.domain;
public class InventoryItem {
    private Product product;
    private int quantity;
    public InventoryItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }
    public int getQuantity() { return quantity; }
    public void reduce(int amount) { this.quantity -= amount; }
}