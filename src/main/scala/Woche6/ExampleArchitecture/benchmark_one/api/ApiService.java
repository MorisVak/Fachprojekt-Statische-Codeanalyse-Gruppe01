package Woche6.ExampleArchitecture.benchmark_one.api;

import Woche6.ExampleArchitecture.benchmark_one.shared.SharedException;


public interface ApiService {
    String fetchData() throws SharedException, SharedException;
}