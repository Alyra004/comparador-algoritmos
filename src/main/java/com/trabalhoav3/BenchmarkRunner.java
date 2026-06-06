package com.trabalhoav3;

import java.util.function.Supplier;

public class BenchmarkRunner {
    
    public static BenchmarkResult runTest(String name, Supplier<Integer> searchFunction) {
        double[] timesMs = new double[3];
        int occurrences = 0;

        for (int i = 0; i < 3; i++) {
            long startTime = System.nanoTime();
            
            occurrences = searchFunction.get();
            
            long endTime = System.nanoTime();
            timesMs[i] = (endTime - startTime) / 1_000_000.0;
        }

        return new BenchmarkResult(name, occurrences, timesMs);
    }
}