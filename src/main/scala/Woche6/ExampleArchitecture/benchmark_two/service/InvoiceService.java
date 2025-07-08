package Woche6.ExampleArchitecture.benchmark_two.service;
import Woche6.ExampleArchitecture.benchmark_two.domain.Invoice;
import Woche6.ExampleArchitecture.benchmark_two.domain.Order;
import Woche6.ExampleArchitecture.benchmark_two.util.Logger;
public class InvoiceService {
    public Invoice createInvoice(Order o) {
        Logger.log("Creating invoice for order of " + o.getProduct().getId());
        return new Invoice(o);
    }
}