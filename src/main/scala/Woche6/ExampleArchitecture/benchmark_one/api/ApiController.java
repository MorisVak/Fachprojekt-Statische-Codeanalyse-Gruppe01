package Woche6.ExampleArchitecture.benchmark_one.api;

import Woche6.ExampleArchitecture.benchmark_one.core.AbstractController;
import Woche6.ExampleArchitecture.benchmark_one.model.ModelMapper;

public class ApiController extends AbstractController {
    private final ModelMapper mapper = new ModelMapper();

    public void handle() {
        log("Handling API request");
        // Mapping example
        // ...
    }
}