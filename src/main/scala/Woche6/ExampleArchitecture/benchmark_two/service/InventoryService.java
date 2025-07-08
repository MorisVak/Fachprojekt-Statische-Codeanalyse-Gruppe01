package Woche6.ExampleArchitecture.benchmark_two.service;
import Woche6.ExampleArchitecture.benchmark_two.domain.InventoryItem;
import Woche6.ExampleArchitecture.benchmark_two.domain.Product;
import Woche6.ExampleArchitecture.benchmark_two.util.Logger;
public class InventoryService {
    private InventoryItem item;
    public InventoryService(InventoryItem item) { this.item = item; }
    public boolean inStock() {
        boolean ok = item.getQuantity() > 0;
        Logger.log("Inventory in stock: " + ok);
        return ok;
    }
}