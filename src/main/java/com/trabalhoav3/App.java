package com.trabalhoav3;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        FileManager fileManager = new FileManager("samples");
        Scanner scanner = new Scanner(System.in);

        System.out.println("--- Gerenciador de Arquivos ---");

        List<Path> files = fileManager.getFilesList();

        if (files.isEmpty()) {
            System.out.println("A pasta 'samples' está vazia!");
            System.out.println("Crie alguns arquivos .txt lá dentro e rode o programa novamente.");
            scanner.close();
            return;
        }

        System.out.println("Arquivos disponíveis:");
        for (int i = 0; i < files.size(); i++) {
            System.out.println((i + 1) + " - " + files.get(i).getFileName());
        }

        boolean isValid = false;
        List<Path> selectedPaths = new ArrayList<>();

        while (!isValid) {
            System.out.print("\nDigite o número dos arquivos que deseja selecionar separados por vírgula: ");
            String input = scanner.nextLine();

            try {
                List<Path> caminhosSelecionados = new ArrayList<>();
                String[] partes = input.split(",");

                for (String parte : partes) {
                    int index = Integer.parseInt(parte.trim());

                    if (index < 1 || index > files.size()) {
                        throw new NumberFormatException(); 
                    } else {
                        caminhosSelecionados.add(fileManager.getPath(index));
                    }
                }

                isValid = true;

                System.out.println("\n[SUCESSO!] Você selecionou " + caminhosSelecionados.size() + " arquivo(s):");
                for (Path caminho : caminhosSelecionados) {
                    System.out.println(" - " + caminho.getFileName());
                }

                System.out.print("\nDigite a palavra que deseja buscar: ");
                String palavraAlvo = scanner.nextLine().toLowerCase(); 
                byte[] palavraEmBytes = palavraAlvo.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                
                System.out.print("Deseja realizar a busca EXATA (considerando fronteiras de palavras)? (S/N): ");
                boolean buscaExata = scanner.nextLine().trim().equalsIgnoreCase("S");

                List<java.nio.ByteBuffer> buffers = new ArrayList<>();
                for (Path caminho : caminhosSelecionados) {
                    java.nio.ByteBuffer b = BufferMapper.mapFileToMemory(caminho);
                    if (b != null) {
                        buffers.add(b);
                    }
                }

                if (!buffers.isEmpty()) {
                    System.out.println("\n--- Iniciando Bateria de Testes (3 Amostras) ---");

                    // 1. Busca Serial (CPU)
                    BenchmarkResult serialResult = BenchmarkRunner.runTest("Serial CPU", () -> {
                        return SerialCpuSearcher.search(buffers, palavraEmBytes, buscaExata);
                    });
                    System.out.println(serialResult);

                    // 2. Busca Paralela (CPU) - Potência Máxima
                    int maxThreads = Runtime.getRuntime().availableProcessors();
                    BenchmarkResult parallelMaxResult = BenchmarkRunner.runTest("Parallel CPU (" + maxThreads + " Threads)", () -> {
                        return ParallelCpu.search(buffers, palavraEmBytes, buscaExata, maxThreads);
                    });
                    System.out.println(parallelMaxResult);

                    // 3. Busca Paralela (CPU) - Análise de Escalabilidade
                    int halfThreads = Math.max(1, maxThreads / 2);
                    BenchmarkResult parallelHalfResult = BenchmarkRunner.runTest("Parallel CPU (" + halfThreads + " Threads)", () -> {
                        return ParallelCpu.search(buffers, palavraEmBytes, buscaExata, halfThreads);
                    });
                    System.out.println(parallelHalfResult);

                    // 4. Busca Massiva (GPU - OpenCL)
                    BenchmarkResult gpuResult = BenchmarkRunner.runTest("Parallel GPU", () -> {
                        return ParallelGpu.search(buffers, palavraEmBytes, buscaExata);
                    });
                    System.out.println(gpuResult);
                }

            } catch (NumberFormatException e) {
                System.out.println("Erro: Entrada inválida. Por favor, digite apenas números.");
                selectedPaths.clear();
            }
        }

        scanner.close();
    }
}