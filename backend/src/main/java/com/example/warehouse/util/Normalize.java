package com.example.warehouse.util;

public class Normalize {

    private Normalize() {}

    public static String normalize(String s) {
        if (s == null) return null;
        String v = s.trim().replaceAll("\\s+", " ");
        return v.isBlank() ? null : v;
    }
}