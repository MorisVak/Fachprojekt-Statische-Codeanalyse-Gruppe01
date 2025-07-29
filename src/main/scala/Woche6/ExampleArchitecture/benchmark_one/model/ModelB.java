package Woche6.ExampleArchitecture.benchmark_one.model;

import Woche6.ExampleArchitecture.benchmark_one.helper.HelperA;

public class ModelB {
    private final long timestamp;
    private final String value;
    private final HelperA helper;

    public ModelB(long timestamp, String value, HelperA helper) {
        this.timestamp = timestamp;
        this.value = value;
        this.helper = helper;
    }

    public long getTimestamp() { return timestamp; }
    public String getValue() { return value; }
    public HelperA getHelper() { return helper; }
}