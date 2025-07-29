package Woche6.ExampleArchitecture.benchmark_two.service;
import Woche6.ExampleArchitecture.benchmark_two.domain.Product;
import Woche6.ExampleArchitecture.benchmark_two.util.Logger;
public class ProductService {
    public Product createProduct(String id) {
        Logger.log("Creating product " + id);
        return new Product(id);
    }
}