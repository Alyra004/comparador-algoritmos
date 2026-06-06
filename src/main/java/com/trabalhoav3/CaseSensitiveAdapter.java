package com.trabalhoav3;

public class CaseSensitiveAdapter {
    public static byte toLowerCase(byte b) {
        if (b >= 'A' && b <= 'Z') {
            return (byte) (b + 32);
        }
        return b;
    }
}
