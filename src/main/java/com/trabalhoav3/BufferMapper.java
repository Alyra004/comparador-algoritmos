package com.trabalhoav3;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.io.IOException;

public class BufferMapper {
    public static ByteBuffer mapFileToMemory(Path filePath) {
        try(FileChannel channel = FileChannel.open(filePath, StandardOpenOption.READ)) {
            long fileSize = channel.size();
            ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);

            return buffer;

        } catch (IOException e) {
            System.out.println("Erro ao tentar mapear o arquivo: " + filePath.getFileName());
            System.out.println("Detalhes técnicos: " + e.getMessage());
            return null;
        }
    }
}