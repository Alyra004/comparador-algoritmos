package com.trabalhoav3;

import java.util.function.Supplier;

public class TimerBenchmark {
    public static void timeRun(String testName, Supplier<Integer> algorithm){
        long startTime = System.nanoTime();
        int ocorrencias = algorithm.get();
        long endTime = System.nanoTime();
        double timeMs = (endTime - startTime) / 1_000_000.0;
        System.out.printf("[%s] Resultado: %d ocorrências encontradas em %.4f ms%n", testName, ocorrencias, timeMs);
    }
}