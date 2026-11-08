package org.example;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class AlgorithmTest {

    private final List<EditDistance.Solver> solvers = List.of(
            new EditDistance.Recursive(),
            new EditDistance.Table(),
            new EditDistance.TwoRows()
    );

    private final List<StringSearch.Matcher> matchers = List.of(
            new StringSearch.Naive(),
            new StringSearch.KMP()
    );

    @Test
    public void testEditDistanceReferenceValues() {
        for (EditDistance.Solver solver : solvers) {
            assertEquals(3, solver.distance("kitten", "sitting"), solver.name() + " failed on kitten/sitting");
            assertEquals(2, solver.distance("flaw", "lawn"), solver.name() + " failed on flaw/lawn");
            assertEquals(5, solver.distance("intention", "execution"), solver.name() + " failed on intention/execution");
            assertEquals(3, solver.distance("", "abc"), solver.name() + " failed on empty/abc");
            assertEquals(3, solver.distance("abc", ""), solver.name() + " failed on abc/empty");
            assertEquals(0, solver.distance("abc", "abc"), solver.name() + " failed on abc/abc");
            assertEquals(0, solver.distance("", ""), solver.name() + " failed on empty/empty");
        }
    }

    @Test
    public void testEditDistanceRandomCrossCheck() {
        Random rnd = new Random(42);
        EditDistance.Solver rec = new EditDistance.Recursive();
        EditDistance.Solver tab = new EditDistance.Table();
        EditDistance.Solver two = new EditDistance.TwoRows();

        for (int t = 0; t < 100; t++) {
            String a = randomString(rnd, rnd.nextInt(8), "ABCD");
            String b = randomString(rnd, rnd.nextInt(8), "ABCD");

            int d1 = rec.distance(a, b);
            int d2 = tab.distance(a, b);
            int d3 = two.distance(a, b);

            assertEquals(d1, d2, "Mismatch Recursive vs Table on " + a + ", " + b);
            assertEquals(d2, d3, "Mismatch Table vs TwoRows on " + a + ", " + b);
        }
    }

    @Test
    public void testLpsReferenceValues() {
        assertArrayEquals(new int[]{0, 1, 2, 0, 1, 2, 3, 3}, StringSearch.lps("AAACAAAA"));
        assertArrayEquals(new int[]{0, 0, 1, 2, 0, 1, 2, 3, 4}, StringSearch.lps("ABABCABAB"));
    }

    @Test
    public void testStringSearchReferenceAndOverlaps() {
        for (StringSearch.Matcher matcher : matchers) {
            assertEquals(List.of(10), matcher.search("ABABDABACDABABCABAB", "ABABCABAB"), matcher.name());
            assertEquals(List.of(0, 1, 2, 3), matcher.search("AAAAA", "AA"), matcher.name());
            assertEquals(List.of(0, 2, 4), matcher.search("ABABABA", "ABA"), matcher.name());
            assertEquals(List.of(), matcher.search("ABCDEFG", "XYZ"), matcher.name());
        }
    }

    @Test
    public void testStringSearchRandomCrossCheck() {
        Random rnd = new Random(42);
        StringSearch.Matcher naive = new StringSearch.Naive();
        StringSearch.Matcher kmp = new StringSearch.KMP();

        for (int t = 0; t < 100; t++) {
            int textLen = 20 + rnd.nextInt(100);
            int patLen = 1 + rnd.nextInt(6);
            String text = randomString(rnd, textLen, "ABC");
            String pattern = randomString(rnd, patLen, "ABC");

            List<Integer> resNaive = naive.search(text, pattern);
            List<Integer> resKmp = kmp.search(text, pattern);
            assertEquals(resNaive, resKmp, "Mismatch on text=" + text + ", pattern=" + pattern);
        }
    }

    @Test
    public void testStringSearchEdgeCases() {
        for (StringSearch.Matcher matcher : matchers) {
            assertThrows(IllegalArgumentException.class, () -> matcher.search("ABC", ""));
            assertThrows(IllegalArgumentException.class, () -> matcher.search("ABC", null));
            assertEquals(List.of(), matcher.search("SHORT", "LONGER_PATTERN"));
            assertEquals(List.of(), matcher.search("", "A"));
        }
        assertThrows(IllegalArgumentException.class, () -> StringSearch.lps(""));
    }

    private String randomString(Random rnd, int len, String alphabet) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
        }
        return sb.toString();
    }
}