package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StringSearch {

    public interface Matcher {
        String name();
        List<Integer> search(String text, String pattern, Metrics metrics);

        default List<Integer> search(String text, String pattern) {
            return search(text, pattern, new Metrics());
        }
    }

    private static void validateInputs(String text, String pattern) {
        if (text == null || pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("Text must not be null and pattern must not be empty");
        }
    }

    public static int[] lps(String p) {
        return lps(p, null);
    }

    public static int[] lps(String p, Metrics metrics) {
        if (p == null || p.isEmpty()) {
            throw new IllegalArgumentException("Pattern must not be null or empty");
        }
        int m = p.length();
        int[] lps = new int[m];
        int len = 0;
        int i = 1;

        while (i < m) {
            if (metrics != null) {
                metrics.addCharComparison();
            }
            if (p.charAt(i) == p.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    len = lps[len - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }
        return lps;
    }

    public static class Naive implements Matcher {
        @Override
        public String name() {
            return "Search_Naive";
        }

        @Override
        public List<Integer> search(String text, String pattern, Metrics metrics) {
            validateInputs(text, pattern);
            int n = text.length();
            int m = pattern.length();
            if (m > n) {
                return Collections.emptyList();
            }

            List<Integer> matches = new ArrayList<>();
            for (int i = 0; i <= n - m; i++) {
                int j = 0;
                while (j < m) {
                    if (metrics != null) {
                        metrics.addCharComparison();
                    }
                    if (text.charAt(i + j) != pattern.charAt(j)) {
                        break;
                    }
                    j++;
                }
                if (j == m) {
                    matches.add(i);
                }
            }
            return matches;
        }
    }

    public static class KMP implements Matcher {
        @Override
        public String name() {
            return "Search_KMP";
        }

        @Override
        public List<Integer> search(String text, String pattern, Metrics metrics) {
            validateInputs(text, pattern);
            int n = text.length();
            int m = pattern.length();
            if (m > n) {
                return Collections.emptyList();
            }

            int[] lps = lps(pattern, metrics);
            List<Integer> matches = new ArrayList<>();
            int i = 0;
            int j = 0;

            while (i < n) {
                if (metrics != null) {
                    metrics.addCharComparison();
                }
                if (text.charAt(i) == pattern.charAt(j)) {
                    i++;
                    j++;
                    if (j == m) {
                        matches.add(i - j);
                        j = lps[j - 1];
                    }
                } else {
                    if (j != 0) {
                        j = lps[j - 1];
                    } else {
                        i++;
                    }
                }
            }
            return matches;
        }
    }
}