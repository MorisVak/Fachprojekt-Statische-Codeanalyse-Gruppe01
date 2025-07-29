package Woche6.ExampleArchitecture.benchmark_one.api;

import Woche6.ExampleArchitecture.benchmark_one.core.CoreService;
import Woche6.ExampleArchitecture.benchmark_one.shared.SharedException;

public class ApiServiceImpl implements ApiService {
    private final CoreService core = new Woche6.ExampleArchitecture.benchmark_one.core.CoreServiceImpl();

    @Override
    public String fetchData() throws SharedException {
        return core.getData();
    }
}