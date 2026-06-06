package com.trabalhoav3;

public class BenchmarkResult {
    private final String algorithmName;
    private final int occurrences;
    private final double[] timesMs;
    private final double averageTimeMs;

    public BenchmarkResult(String algorithmName, int occurrences, double[] timesMs) {
        this.algorithmName = algorithmName;
        this.occurrences = occurrences;
        this.timesMs = timesMs;
        
        double sum = 0;
        for (double time : timesMs) {
            sum += time;
        }
        this.averageTimeMs = sum / timesMs.length;
    }

    public String getAlgorithmName() { return algorithmName; }
    public int getOccurrences() { return occurrences; }
    public double[] getTimesMs() { return timesMs; }
    public double getAverageTimeMs() { return averageTimeMs; }

    @Override
    public String toString() {
        return String.format("[%s] %d ocorrências | Tempos: [%.2f, %.2f, %.2f] ms | Média: %.2f ms",
                algorithmName, occurrences, timesMs[0], timesMs[1], timesMs[2], averageTimeMs);
    }
}