package Woche6.ExampleArchitecture.benchmark_one.core;

import Woche6.ExampleArchitecture.benchmark_one.shared.SharedException;

public interface CoreService {
    String getData() throws SharedException;
}