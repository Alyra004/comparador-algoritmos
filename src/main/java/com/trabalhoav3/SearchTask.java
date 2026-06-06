package com.trabalhoav3;

import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

public class SearchTask implements Callable<Integer>{
    private final ByteBuffer buffer;
    private final byte[] targetWord;
    private final boolean exactSearch;
    private final int start;
    private final int end;

    public SearchTask(ByteBuffer originalBuffer, byte[] targetWord, boolean exactSearch, int start, int end) {
        this.buffer = originalBuffer.duplicate(); 
        
        this.targetWord = targetWord;
        this.exactSearch = exactSearch;
        this.start = start;
        this.end = end;
        
        this.buffer.position(start);
    }

    @Override
    public Integer call() {
        int occurences = 0;
        int wordIndex = 0;
        
        byte previousByte = (start > 0) ? buffer.get(start - 1) : (byte) ' ';

        while (buffer.position() < end && buffer.hasRemaining()) {
            
            int currentPosition = buffer.position();
            byte b = buffer.get();
            
            if (CaseSensitiveAdapter.toLowerCase(b) == targetWord[wordIndex]) {
                wordIndex++;
                
                if (wordIndex == targetWord.length) {
                    if (exactSearch) {
                        boolean priorNotAlphanumeric = SearchUtils.isNotAlphanumeric(previousByte);
                        byte nextByte = buffer.hasRemaining() ? buffer.get(buffer.position()) : (byte) ' ';
                        boolean postNotAlphanumeric = SearchUtils.isNotAlphanumeric(nextByte);
                        
                        if (priorNotAlphanumeric && postNotAlphanumeric) {
                            occurences++;
                        }
                    } else {
                        occurences++;
                    }
                    wordIndex = 0;
                }
            } else {
                if (wordIndex > 0) {
                    buffer.position(currentPosition - wordIndex + 1);
                }
                wordIndex = 0;
            }

            if (wordIndex == 0) {
                previousByte = b;
            }
        }

        return occurences;
    }
}
