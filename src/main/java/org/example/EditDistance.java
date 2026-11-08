package org.example;

public class EditDistance {

    public interface Solver {
        String name();
        int distance(String a, String b, Metrics metrics);

        default int distance(String a, String b) {
            return distance(a, b, new Metrics());
        }
    }

    // State: dp(i, j) is the minimum edit distance between prefixes a[0..i-1] and b[0..j-1].
    // Transition: if a[i-1] == b[j-1] then dp(i-1, j-1), else 1 + min(dp(i-1, j), dp(i, j-1), dp(i-1, j-1)).
    // Base case: dp(i, 0) = i and dp(0, j) = j (converting to/from an empty prefix).
    public static class Recursive implements Solver {
        @Override
        public String name() {
            return "EditDistance_Recursive";
        }

        @Override
        public int distance(String a, String b, Metrics metrics) {
            if (a == null || b == null) {
                throw new IllegalArgumentException("Strings must not be null");
            }
            return helper(a, b, a.length(), b.length(), metrics);
        }

        private int helper(String a, String b, int i, int j, Metrics metrics) {
            if (metrics != null) {
                metrics.addDpOperation();
            }
            if (i == 0) return j;
            if (j == 0) return i;

            if (metrics != null) {
                metrics.addCharComparison();
            }
            if (a.charAt(i - 1) == b.charAt(j - 1)) {
                return helper(a, b, i - 1, j - 1, metrics);
            }

            int insert = helper(a, b, i, j - 1, metrics);
            int delete = helper(a, b, i - 1, j, metrics);
            int replace = helper(a, b, i - 1, j - 1, metrics);

            return 1 + Math.min(insert, Math.min(delete, replace));
        }
    }

    // State: dp[i][j] stores the minimum edit distance between prefixes a[0..i-1] and b[0..j-1].
    // Transition: dp[i][j] = dp[i-1][j-1] if a[i-1] == b[j-1], else 1 + min(dp[i-1][j], dp[i][j-1], dp[i-1][j-1]).
    // Base case: dp[i][0] = i for all 0 <= i <= n, and dp[0][j] = j for all 0 <= j <= m.
    public static class Table implements Solver {
        @Override
        public String name() {
            return "EditDistance_Table";
        }

        @Override
        public int distance(String a, String b, Metrics metrics) {
            if (a == null || b == null) {
                throw new IllegalArgumentException("Strings must not be null");
            }
            int n = a.length();
            int m = b.length();
            int[][] dp = new int[n + 1][m + 1];

            for (int i = 0; i <= n; i++) {
                dp[i][0] = i;
            }
            for (int j = 0; j <= m; j++) {
                dp[0][j] = j;
            }

            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= m; j++) {
                    if (metrics != null) {
                        metrics.addDpOperation();
                        metrics.addCharComparison();
                    }
                    if (a.charAt(i - 1) == b.charAt(j - 1)) {
                        dp[i][j] = dp[i - 1][j - 1];
                    } else {
                        dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
                    }
                }
            }
            return dp[n][m];
        }
    }

    // State: prev[j] and curr[j] represent rows i-1 and i of the DP state for prefixes a[0..i-1] and b[0..j-1].
    // Transition: curr[j] = prev[j-1] if a[i-1] == b[j-1], else 1 + min(prev[j], curr[j-1], prev[j-1]).
    // Base case: prev[j] = j for row 0, and curr[0] = i at the start of each row i.
    public static class TwoRows implements Solver {
        @Override
        public String name() {
            return "EditDistance_TwoRows";
        }

        @Override
        public int distance(String a, String b, Metrics metrics) {
            if (a == null || b == null) {
                throw new IllegalArgumentException("Strings must not be null");
            }
            int n = a.length();
            int m = b.length();
            int[] prev = new int[m + 1];
            int[] curr = new int[m + 1];

            for (int j = 0; j <= m; j++) {
                prev[j] = j;
            }

            for (int i = 1; i <= n; i++) {
                curr[0] = i;
                for (int j = 1; j <= m; j++) {
                    if (metrics != null) {
                        metrics.addDpOperation();
                        metrics.addCharComparison();
                    }
                    if (a.charAt(i - 1) == b.charAt(j - 1)) {
                        curr[j] = prev[j - 1];
                    } else {
                        curr[j] = 1 + Math.min(prev[j - 1], Math.min(prev[j], curr[j - 1]));
                    }
                }
                int[] temp = prev;
                prev = curr;
                curr = temp;
            }
            return prev[m];
        }
    }
}