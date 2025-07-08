package Woche6.ExampleArchitecture.benchmark_one.core;

import Woche6.ExampleArchitecture.benchmark_one.shared.SharedUtil;

public class CoreManager {
    private static final CoreManager INSTANCE = new CoreManager();
    private final CoreService service = new CoreServiceImpl();

    private CoreManager() {}

    public static CoreManager getInstance() {
        return INSTANCE;
    }

    public String fetchAndFormat() {
        String data;
        try {
            data = service.getData();
        } catch (Exception e) {
            return SharedUtil.format("Error: %s", e.getMessage());
        }
        return SharedUtil.format("Result: %s", data);
    }
}