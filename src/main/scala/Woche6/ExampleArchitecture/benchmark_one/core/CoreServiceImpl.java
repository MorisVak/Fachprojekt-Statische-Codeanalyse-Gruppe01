package Woche6.ExampleArchitecture.benchmark_one.core;

import Woche6.ExampleArchitecture.benchmark_one.shared.SharedException;
import Woche6.ExampleArchitecture.benchmark_one.api.ApiClient;

public class CoreServiceImpl implements CoreService {
    private final ApiClient client = new ApiClient();

    @Override
    public String getData() throws SharedException {
        return client.callApi();
    }
}