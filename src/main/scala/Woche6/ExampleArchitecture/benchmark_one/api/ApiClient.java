package Woche6.ExampleArchitecture.benchmark_one.api;

import Woche6.ExampleArchitecture.benchmark_one.utils.HttpHelper;

public class ApiClient {
    public String callApi() {
        return HttpHelper.get("https://api.example.com/data");
    }
}