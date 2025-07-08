package Woche6.ExampleArchitecture.benchmark_one.helper;

import Woche6.ExampleArchitecture.benchmark_one.utils.CryptoUtil;
import Woche6.ExampleArchitecture.benchmark_one.shared.Version;

public class HelperB {
    public String secure(String data) {
        String hash = CryptoUtil.hash(data);
        return Version.V1 + ":" + hash;
    }
}