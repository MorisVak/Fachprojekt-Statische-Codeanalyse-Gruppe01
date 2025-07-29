package Woche6.ExampleArchitecture.benchmark_two.service;
import Woche6.ExampleArchitecture.benchmark_two.domain.Invoice;
import Woche6.ExampleArchitecture.benchmark_two.util.Logger;
public class RepositoryManager {
    public void saveInvoice(Invoice inv) {
        Logger.log("Saving invoice with total " + inv.calculateTotal());
    }
}