package Woche6.ExampleArchitecture.benchmark_one.helper;

import Woche6.ExampleArchitecture.benchmark_one.shared.SharedUtil;

public class HelperA {
    public String process(String input) {
        return SharedUtil.format("Processed: %s", input);
    }
}