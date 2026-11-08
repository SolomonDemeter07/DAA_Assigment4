# Assignment 4 — Dynamic Programming and String Algorithms

## Project Overview
This project implements and benchmarks classic Dynamic Programming and String Matching algorithms for the City Search & Spell-Check scenario:
- **Edit Distance (`EditDistance.java`):** Implemented in three versions sharing a unified `Solver` interface: `Recursive` (direct recursion), `Table` ($\Theta(n \cdot m)$ time and space), and `TwoRows` ($\Theta(n \cdot m)$ time, $\Theta(m)$ space).
- **String Search (`StringSearch.java`):** Implemented with a unified `Matcher` interface for `Naive` and `KMP` search (with public `lps(String p)` prefix-function builder), supporting overlapping matches and edge-case validation.
- **Metrics & Experiment (`Metrics.java`, `Benchmark.java`, `PlotGenerator.java`):** Tracks internal counters (`dp_ops`, `char_comparisons`) and median execution time across reproducible best- and worst-case inputs, exporting `results/results.csv` and generating charts in `results/plots/`.

## How to Build the Project
Run in terminal:
mvn clean test-compile

Or in IntelliJ IDEA: **Build -> Build Project** (`Ctrl + F9`).

## How to Run the Tests
Run in terminal:
mvn test

Or open `src/test/java/org/example/AlgorithmTest.java` in IntelliJ IDEA and click the green **Run** icon.

## How to Run the Experiment & Generate Plots
1. Run `src/main/java/org/example/Benchmark.java` — executes the experiment with a fixed seed and generates `results/results.csv`.
2. Run `src/main/java/org/example/PlotGenerator.java` — reads `results/results.csv` and outputs three PNG charts into `results/plots/`.

