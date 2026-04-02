package com.scalelink.util;

public class UrlValidator {

    public static boolean isValidUrl(String url) {
        if (url == null || url.isBlank()) return false;
        return url.startsWith("http://") || url.startsWith("https://");
    }

    public static String normalizeUrl(String url) {
        if (url == null) return null;
        return url.trim().toLowerCase().startsWith("http") ? url.trim() : "https://" + url.trim();
    }
}
