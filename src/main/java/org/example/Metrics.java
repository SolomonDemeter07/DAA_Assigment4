package org.example;

public class Metrics {
    private long dpOperations = 0;
    private long charComparisons = 0;
    private long startTime = 0;
    private long endTime = 0;

    public void addDpOperation() {
        dpOperations++;
    }

    public void addCharComparison() {
        charComparisons++;
    }

    public void startTimer() {
        startTime = System.nanoTime();
    }

    public void stopTimer() {
        endTime = System.nanoTime();
    }

    public double getTimeMs() {
        return (endTime - startTime) / 1_000_000.0;
    }

    public long getDpOperations() {
        return dpOperations;
    }

    public long getCharComparisons() {
        return charComparisons;
    }

    public void reset() {
        dpOperations = 0;
        charComparisons = 0;
        startTime = 0;
        endTime = 0;
    }
}