package Woche6.ExampleArchitecture.benchmark_two.service;
import Woche6.ExampleArchitecture.benchmark_two.domain.*;
public class ApplicationManager {
    public void run() {
        UserService us = new UserService();
        ProductService ps = new ProductService();
        OrderService os = new OrderService();
        InvoiceService is = new InvoiceService();
        NotificationService ns = new NotificationService();
        User u = us.createUser("Alice");
        Product p = ps.createProduct("P1");
        Order o = os.createOrder(u, p);
        Invoice inv = is.createInvoice(o);
        ns.notify(u, "Your total is " + inv.calculateTotal());
    }
}