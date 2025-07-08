package Woche6.ExampleArchitecture.benchmark_one.model;

public class ModelMapper {
    public ModelB toModelB(ModelA a) {
        return new ModelB(System.currentTimeMillis(), a.getName(), a.getHelper());
    }

    public ModelA toModelA(ModelB b) {
        return new ModelA(b.getValue(), "mapped", null);
    }
}