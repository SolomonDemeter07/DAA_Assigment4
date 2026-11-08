package org.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Benchmark {
    private static final int[] REC_SIZES = {3, 6, 9, 12};
    private static final int[] DP_SIZES = {500, 1000, 2000, 4000};
    private static final int[] SEARCH_SIZES = {25000, 50000, 100000, 200000};
    private static final int PATTERN_LEN = 400;
    private static final int RUNS = 5;

    public static void main(String[] args) throws IOException {
        File dir = new File("results");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        EditDistance.Solver rec = new EditDistance.Recursive();
        EditDistance.Solver table = new EditDistance.Table();
        EditDistance.Solver twoRows = new EditDistance.TwoRows();

        StringSearch.Matcher naive = new StringSearch.Naive();
        StringSearch.Matcher kmp = new StringSearch.KMP();

        try (FileWriter writer = new FileWriter("results/results.csv")) {
            writer.write("algorithm,input,n,m,time_ms,dp_ops,char_comparisons,result\n");

            for (String inputType : new String[]{"identical", "worst_mismatch"}) {
                for (int n : REC_SIZES) {
                    String[] pair = generateEditPair(inputType, n);
                    for (EditDistance.Solver solver : List.of(rec, table, twoRows)) {
                        RunRecord recOut = measure(() -> {
                            Metrics met = new Metrics();
                            int dist = solver.distance(pair[0], pair[1], met);
                            return new TaskOut(met, dist);
                        });
                        writeCsvRow(writer, solver.name(), inputType, n, n, recOut);
                    }
                }
            }

            for (String inputType : new String[]{"identical", "worst_mismatch"}) {
                for (int n : DP_SIZES) {
                    String[] pair = generateEditPair(inputType, n);
                    for (EditDistance.Solver solver : List.of(table, twoRows)) {
                        RunRecord dpOut = measure(() -> {
                            Metrics met = new Metrics();
                            int dist = solver.distance(pair[0], pair[1], met);
                            return new TaskOut(met, dist);
                        });
                        writeCsvRow(writer, solver.name(), inputType, n, n, dpOut);
                    }
                }
            }

            for (String inputType : new String[]{"random_text", "periodic_worst"}) {
                for (int n : SEARCH_SIZES) {
                    String[] tp = generateSearchPair(inputType, n, PATTERN_LEN);
                    for (StringSearch.Matcher matcher : List.of(naive, kmp)) {
                        RunRecord searchOut = measure(() -> {
                            Metrics met = new Metrics();
                            List<Integer> matches = matcher.search(tp[0], tp[1], met);
                            return new TaskOut(met, matches.size());
                        });
                        writeCsvRow(writer, matcher.name(), inputType, n, PATTERN_LEN, searchOut);
                    }
                }
            }
        }
        System.out.println("Benchmark complete! Results saved to results/results.csv");
    }

    private static String[] generateEditPair(String type, int n) {
        Random rnd = new Random(42L + n);
        if (type.equals("identical")) {
            String s = randomString(rnd, n, "ABCDEFGH");
            return new String[]{s, s};
        } else {
            String a = randomString(rnd, n, "ABCDEFGH");
            String b = randomString(rnd, n, "IJKLMNOP");
            return new String[]{a, b};
        }
    }

    private static String[] generateSearchPair(String type, int n, int m) {
        Random rnd = new Random(42L + n);
        if (type.equals("random_text")) {
            String text = randomString(rnd, n, "ABCDEFGH");
            String pattern = randomString(rnd, m, "ABCDEFGH");
            return new String[]{text, pattern};
        } else {
            StringBuilder textSb = new StringBuilder(n);
            for (int i = 0; i < n; i++) {
                textSb.append('A');
            }
            StringBuilder patSb = new StringBuilder(m);
            for (int i = 0; i < m - 1; i++) {
                patSb.append('A');
            }
            patSb.append('B');
            return new String[]{textSb.toString(), patSb.toString()};
        }
    }

    private static String randomString(Random rnd, int len, String alphabet) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    private interface Task {
        TaskOut run();
    }

    private static class TaskOut {
        final Metrics metrics;
        final int result;

        TaskOut(Metrics metrics, int result) {
            this.metrics = metrics;
            this.result = result;
        }
    }

    private static class RunRecord {
        final double timeMs;
        final Metrics metrics;
        final int result;

        RunRecord(double timeMs, Metrics metrics, int result) {
            this.timeMs = timeMs;
            this.metrics = metrics;
            this.result = result;
        }
    }

    private static RunRecord measure(Task task) {
        RunRecord[] runs = new RunRecord[RUNS];
        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            TaskOut out = task.run();
            long end = System.nanoTime();
            runs[i] = new RunRecord((end - start) / 1_000_000.0, out.metrics, out.result);
        }
        Arrays.sort(runs, (a, b) -> Double.compare(a.timeMs, b.timeMs));
        return runs[RUNS / 2];
    }

    private static void writeCsvRow(FileWriter writer, String algo, String input, int n, int m, RunRecord r) throws IOException {
        writer.write(String.format(Locale.US, "%s,%s,%d,%d,%.4f,%d,%d,%d%n",
                algo, input, n, m, r.timeMs, r.metrics.getDpOperations(), r.metrics.getCharComparisons(), r.result));
    }
}