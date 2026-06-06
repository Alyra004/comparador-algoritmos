package com.trabalhoav3;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.Collections;
import java.io.IOException;

public class FileManager {
    private final Path samplesPath;
    private final List<Path> filePaths;

    public FileManager(String folderName){
        this.samplesPath = Paths.get(folderName);
        this.filePaths = new ArrayList<>();
        refreshFileList();
    }

    public void refreshFileList(){
        filePaths.clear();
        try{
            if (!Files.exists(samplesPath)){
                Files.createDirectories(samplesPath);
            }
            try (Stream<Path> stream = Files.list(samplesPath)) {
                stream.filter(path -> Files.isRegularFile(path))
                    .filter(path -> path.toString().toLowerCase().endsWith(".txt"))
                    .forEach(path -> filePaths.add(path));
            }
        } catch (IOException e) {
            System.out.println("CRITICAL ERROR: Nao foi possivel ler o diretorio!");
            System.out.println("Error details: " + e.getMessage());
        }
    }

    public Path getPath(int fileIndex){
        if (fileIndex < 1 || fileIndex > filePaths.size()) {
        System.out.println("Erro: Índice de arquivo inválido!");
        return null;
        }
        Path desiredPath = filePaths.get(fileIndex-1);
        return desiredPath;
    }

    public List<Path> getFilesList(){
        return Collections.unmodifiableList(filePaths);
    }

    public void addNewFile(Path sourcePath){
        try {
            Path target = samplesPath.resolve(sourcePath.getFileName());
            Files.copy(sourcePath, target);
            refreshFileList();
            
        } catch (IOException e) {
            System.out.println("Erro ao copiar o arquivo!");
            System.out.println("Detalhes: " + e.getMessage());
        }
    }

}
