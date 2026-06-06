package com.trabalhoav3;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class CsvExporter {

    public static void exportCompleto(
            List<BenchmarkResult> resGerais,
            Map<String, Integer> ocorrenciasPorLivro,
            Map<String, List<BenchmarkResult>> perfPorLivro,
            String fileName) {

        String separator = ";";

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("--- RESULTADOS GERAIS (TODOS OS LIVROS) ---");
            writer.println("Algoritmo" + separator +
                           "Ocorrencias Totais" + separator +
                           "Amostra 1 (ms)" + separator +
                           "Amostra 2 (ms)" + separator +
                           "Amostra 3 (ms)" + separator +
                           "Media (ms)");

            for (BenchmarkResult result : resGerais) {
                escreverLinhaResultado(writer, result, result.getOccurrences(), separator);
            }

            writer.println();
            if (ocorrenciasPorLivro != null && !ocorrenciasPorLivro.isEmpty()) {
                writer.println("--- DETALHAMENTO POR LIVRO ---");
                writer.println("Livro" + separator +
                               "Algoritmo" + separator +
                               "Ocorrencias no Livro" + separator +
                               "Amostra 1 (ms)" + separator +
                               "Amostra 2 (ms)" + separator +
                               "Amostra 3 (ms)" + separator +
                               "Media (ms)");

                for (Map.Entry<String, List<BenchmarkResult>> entry : perfPorLivro.entrySet()) {
                    String nomeLivro = entry.getKey();
                    int ocorrencias = ocorrenciasPorLivro.getOrDefault(nomeLivro, 0);

                    for (BenchmarkResult result : entry.getValue()) {
                        double[] times = result.getTimesMs();
                        String line = String.format("%s%s%s%s%d%s%.2f%s%.2f%s%.2f%s%.2f",
                                nomeLivro, separator,
                                result.getAlgorithmName(), separator,
                                ocorrencias, separator,
                                times[0], separator,
                                times[1], separator,
                                times[2], separator,
                                result.getAverageTimeMs());
                        
                        writer.println(line.replace(".", ","));
                    }
                }
            }

            System.out.println("\nArquivo CSV Completo gerado com sucesso: " + fileName);

        } catch (IOException e) {
            System.err.println("Erro ao gerar o arquivo CSV: " + e.getMessage());
        }
    }

    private static void escreverLinhaResultado(PrintWriter writer, BenchmarkResult result, int ocorrencias, String separator) {
        double[] times = result.getTimesMs();
        String line = String.format("%s%s%d%s%.2f%s%.2f%s%.2f%s%.2f",
                result.getAlgorithmName(), separator,
                ocorrencias, separator,
                times[0], separator,
                times[1], separator,
                times[2], separator,
                result.getAverageTimeMs());
        writer.println(line.replace(".", ","));
    }
}