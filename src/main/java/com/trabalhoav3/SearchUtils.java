package com.trabalhoav3;
public class SearchUtils {
    public static boolean isNotAlphanumeric(byte b) {
        if (b < 0) return false;
        if (b >= 'A' && b <= 'Z') return false;
        if (b >= 'a' && b <= 'z') return false;
        if (b >= '0' && b <= '9') return false;
        return true;
    }
}
