package com.trabalhoav3;

import java.nio.ByteBuffer;
import java.util.List;

public class SerialCpuSearcher {
    public static int search(List<ByteBuffer> buffers, byte[] targetWord, boolean exactSearch) {
        
        int totalOccurences = 0;

        for (ByteBuffer buffer : buffers) {
            int occurrences = 0;
            int wordIndex = 0;
            byte previousByte = ' ';

            while (buffer.hasRemaining()) {
            
                int currentPosition = buffer.position();
                byte b = buffer.get();

                if (CaseSensitiveAdapter.toLowerCase(b) == targetWord[wordIndex]){
                    wordIndex++;

                    if(wordIndex == targetWord.length){
                        if(exactSearch){
                            boolean priorNotAlphanumeric = SearchUtils.isNotAlphanumeric(previousByte);
                            byte nextByte = buffer.hasRemaining() ? buffer.get(buffer.position()) : (byte)' ';
                            boolean postNotAlphanumeric = SearchUtils.isNotAlphanumeric(nextByte);

                            if(priorNotAlphanumeric && postNotAlphanumeric){
                                occurrences++;
                            }
                        } else {
                            occurrences++;
                        }
                        wordIndex = 0;
                    }
                } else {
                    if (wordIndex > 0){
                        buffer.position(currentPosition - wordIndex + 1);
                    }
                    wordIndex = 0;
                }

                if (wordIndex == 0){
                    previousByte = b;
                }
                
            }
            buffer.rewind();
            totalOccurences += occurrences;
        }
        return totalOccurences;
    }
}
