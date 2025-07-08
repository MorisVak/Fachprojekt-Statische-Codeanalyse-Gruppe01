package Woche6.ExampleArchitecture.benchmark_one.model;

import Woche6.ExampleArchitecture.benchmark_one.helper.HelperA;

public class ModelA {
    private final String id;
    private final String name;
    private final HelperA helper;

    public ModelA(String id, String name, HelperA helper) {
        this.id = id;
        this.name = name;
        this.helper = helper;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public HelperA getHelper() { return helper; }
}