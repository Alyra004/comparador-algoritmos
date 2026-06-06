package com.trabalhoav3;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ParallelCpu {

    public static int search(List<ByteBuffer> buffers, byte[] targetWord, boolean exactSearch, int numThreads) {
        int totalOccurences = 0;
        //int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(numThreads);

        List<Callable<Integer>> tasks = new ArrayList<>();
        
        for (ByteBuffer buffer : buffers) {
            
            int totalSize = buffer.capacity();
            int sliceSize = totalSize / numThreads;

            for (int i = 0; i < numThreads; i++) {
                int start = i * sliceSize;
                int baseEnd = (i == numThreads - 1) ? totalSize : start + sliceSize;
                int realEnd = Math.min(baseEnd + targetWord.length, totalSize);
                
                tasks.add(new SearchTask(buffer, targetWord, exactSearch, start, realEnd));
            }
        }

        try {
            List<Future<Integer>> results = pool.invokeAll(tasks);

            for (Future<Integer> future : results) {
                totalOccurences += future.get();
            }
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Erro durante a execução paralela: " + e.getMessage());
        } finally {
            pool.shutdown();
        }

        for (ByteBuffer buffer : buffers) {
            buffer.rewind();
        }
        
        return totalOccurences;
    }
}