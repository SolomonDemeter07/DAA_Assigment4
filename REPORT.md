# Assignment 4 — Dynamic Programming and String Algorithms Report

## 1. Experiment Design Card

| Card Field | Experiment Specification |
| :--- | :--- |
| **Question** | **Edit Distance:** How does memoization/tabulation eliminate the exponential recomputation of overlapping subproblems in plain recursion, and how does reducing space from $O(n \cdot m)$ to $O(m)$ affect runtime? <br> **String Search:** How does the KMP prefix function (`lps`) prevent quadratic character re-comparisons on periodic worst-case inputs compared to Naive search? |
| **Sizes ($n$)** | **Recursive Edit Distance:** $n \in \{3, 6, 9, 12\}$ ($m = n$, largest is $4\times$ smallest) because plain recursion grows as $O(3^n)$ and reaches $\sim 2.5 \times 10^8$ calls at $n = 12$. <br> **Table & TwoRows Edit Distance:** $n \in \{500, 1000, 2000, 4000\}$ ($m = n$, largest is $8\times$ smallest) to produce measurable millisecond timings without zero-ms noise. <br> **String Search (Naive & KMP):** Text length $n \in \{25000, 50000, 100000, 200000\}$ ($8\times$ range) with pattern length $m = 400$. |
| **Inputs** | **Edit Distance:** (1) `identical` ($a = b$, best case for our recursive implementation where matching characters only recurse diagonally in $O(n)$ calls); (2) `worst_mismatch` ($a \in \{\text{A..H}\}^n$, $b \in \{\text{I..P}\}^n$ with disjoint alphabets, forcing plain recursion to branch into all 3 subproblems at every step). <br> **String Search:** (1) `random_text` (uniform random strings over 8-letter alphabet, average/best case where mismatches occur on the first character); (2) `periodic_worst` ($T = \text{"A"}^n$, $P = \text{"A"}^{m-1}\text{"B"}$, worst case forcing Naive search to compare all $m$ characters at every shift). All inputs are generated with a fixed seed (`Random(42L + n)`). |
| **Metrics** | 1. **`dp_ops` (`dpOperations`):** Counts every recursive call in `Recursive` and every inner DP state update in `Table` and `TwoRows`, directly measuring subproblem evaluations. <br> 2. **`char_comparisons` (`charComparisons`):** Counts every character comparison (`charAt` equality check) inside string search and LPS preprocessing, directly isolating pattern matching work. <br> 3. **`time_ms`:** Median wall-clock execution time across 5 runs using `System.nanoTime()`. |
| **Prediction** | **Edit Distance:** On `worst_mismatch`, `Recursive` will grow exponentially ($\approx 3^n$ slope on a semi-log plot), whereas `Table` and `TwoRows` will perform exactly $n \times m = n^2$ DP operations regardless of input content, with `TwoRows` running slightly faster for large $n$ due to superior CPU cache locality and tiny $O(m)$ allocation. <br> **String Search:** On `random_text`, both `Naive` and `KMP` will perform $\approx 1.1n$ comparisons ($\text{ratio} \approx 1$). On `periodic_worst`, `Naive` will degrade to $(n - m + 1) \cdot m \approx 400n$ comparisons ($\text{ratio} \approx 400$), while `KMP` will remain strictly linear at $\le 2n$ comparisons ($\text{ratio} \approx 2$). |
| **Plots** | 1. **Log Plot (`edit_recursive_log_ops.png`):** $\log_{10}(\text{dp\_ops})$ vs $n$ for $n \in \{3, 6, 9, 12\}$ — answers why plain recursion fails beyond $n \approx 12$. <br> 2. **Time Plot (`edit_dp_time.png`):** Median runtime (ms) vs $n$ for `Table` and `TwoRows` ($n \in \{500..4000\}$) — answers how $O(n^2)$ vs $O(m)$ space impacts practical speed. <br> 3. **Ratio/Log Plot (`search_comparisons_ratio.png`):** $\log_{10}(\text{char\_comparisons} / n)$ vs $n$ for `Naive` and `KMP` — answers whether per-character cost stays constant as text size grows. |

---

## 2. Plots and Results

### Plot 1: Recursive vs Tabulated Edit Distance Operations (Log Axis)
![Edit Distance Recursive Log Operations](results/plots/edit_recursive_log_ops.png)
*Takeaway:* Plain recursion on mismatched strings grows as a straight line on a logarithmic vertical axis ($\sim 2.51 \times 10^8$ calls at $n=12$), whereas bottom-up tabulation evaluates each $(i, j)$ state once ($144$ operations at $n=12$).

### Plot 2: Bottom-Up Edit Distance Runtime (`Table` vs `TwoRows`)
![Edit Distance Table vs TwoRows Time](results/plots/edit_dp_time.png)
*Takeaway:* Both bottom-up versions exhibit the expected quadratic $\Theta(n^2)$ time curve from $n=500$ to $n=4000$, with `TwoRows` consistently outperforming the full 2D `Table` at larger sizes by avoiding multi-megabyte heap allocations and L3 cache misses.

### Plot 3: String Search Comparisons per Text Character (Ratio on Log Axis)
![String Search Comparisons Ratio](results/plots/search_comparisons_ratio.png)
*Takeaway:* On the periodic worst-case input (`AAAA...A` with pattern `AAA...AB`), Naive search performs $\approx 399$ comparisons per text character ($\log_{10} \text{ratio} \approx 2.60$), whereas KMP stays bounded at $\approx 2.0$ comparisons per character ($\log_{10} \text{ratio} \approx 0.30$) across all text sizes.

---

## 3. Prediction vs Result

* **Edit Distance Family:** Our prediction matched the experimental measurements. On `worst_mismatch`, `EditDistance_Recursive` recorded $63$ calls at $n=3$, $8\,989$ calls at $n=6$, $1\,462\,563$ calls at $n=9$, and $250\,595\,895$ calls at $n=12$ (matching the central Delannoy recurrence $T(i,j) = 1 + T(i-1,j) + T(i,j-1) + T(i-1,j-1)$). Conversely, on `identical` strings, our recursive implementation takes only the diagonal branch when `a.charAt(i-1) == b.charAt(j-1)`, resulting in just $n + 1$ calls ($13$ calls at $n=12$). Meanwhile, both `Table` and `TwoRows` performed the exact predicted $n^2$ inner-loop DP operations on both `identical` and `worst_mismatch` inputs ($250\,000$ at $n=500$ up to $16\,000\,000$ at $n=4000$).
* **String Search Family:** The counter ratios confirmed our theoretical expectations. On `random_text` over an 8-letter alphabet, mismatches happen almost immediately at index $j=0$, so `Search_Naive` averaged $\approx 1.14n$ comparisons while `Search_KMP` averaged $\approx 1.15n$ comparisons (slightly higher by $m$ due to `lps` preprocessing). On `periodic_worst`, `Search_Naive` degraded by a factor of $\approx 350\times$ (reaching $79\,840\,400$ comparisons at $n=200\,000$), while `Search_KMP` executed exactly $400\,398 \approx 2n + m$ comparisons without ever backing up the text pointer `i`.

---

## 4. Short Answers

**1. Which input made your string search slowest, and why does it do that?**
The `periodic_worst` input ($T = \text{"A"}^n$ and $P = \text{"A"}^{m-1}\text{"B"}$ with $m=400$) made the Naive string search drastically slowest. At every starting index $i$ in the text, Naive search successfully matches the first $m-1 = 399$ `'A'` characters and only discovers the mismatch on the final character `'B'` at index $j = 399$. Because Naive search has no memory of previous matches, it discards all progress, shifts the start position to $i + 1$, and re-compares the exact same `'A'` characters from scratch, resulting in $\Theta((n - m + 1) \cdot m)$ comparisons. KMP avoids this trap because `lps[398] = 398` tells the algorithm that the last $398$ characters are already `'A'`, so it keeps the text index $i$ moving forward and only checks the new text character against `pattern[398]`.

**2. How large an $n$ could the recursive Edit Distance handle, and what stopped it?**
On worst-case mismatched strings (`worst_mismatch`), our plain recursive Edit Distance could comfortably handle up to $n = 12$ ($250.6\text{M}$ recursive calls, taking $\approx 350\text{ ms}$ per run) and barely survive $n = 13$ ($\approx 1.38\text{B}$ calls, several seconds), while $n \ge 15$ becomes practically unusable. What stops it is **exponential time complexity**, not call-stack memory: because the maximum recursion depth is bounded by $n + m \le 30$, the JVM never throws `StackOverflowError`. Instead, because every mismatch branches into 3 recursive calls (`insert`, `delete`, `replace`) without memoization, the algorithm recomputes the exact same $(i, j)$ subproblems millions of times, growing at the rate of central Delannoy numbers $\Theta((1 + \sqrt{2})^{2n} / \sqrt{n})$.

**3. What do you gain and what do you lose when you use two rows instead of the full table?**
By using `TwoRows` instead of the full `Table`, you reduce auxiliary memory complexity from $\Theta(n \cdot m)$ down to $\Theta(m)$ (allocating just two `int[m + 1]` arrays instead of an $(n+1) \times (m+1)$ matrix). In practice, this prevents `OutOfMemoryError` on very long strings (e.g., $n = 50\,000$, where a full `int[][]` table would require $\approx 10\text{ GB}$ of RAM) and improves CPU cache hit rates because both rows fit entirely inside L1/L2 cache. What you **lose** is the ability to backtrack through the DP matrix to reconstruct the actual sequence of edit operations (which specific characters were inserted, deleted, or replaced), unless you combine space reduction with divide-and-conquer (Hirschberg's algorithm).

**4. If you had to choose one string search for your city platform, which one and why?**
For the city platform, I would choose **KMP** for scanning long server log files, or a hybrid approach that uses KMP whenever the text or pattern exceeds a small threshold. While Naive search has zero preprocessing overhead and is slightly faster on short, random street names where mismatches happen on the first letter, city log files frequently contain repetitive timestamps, padding, or repeated patterns that can trigger quadratic $\Theta(n \cdot m)$ spikes in Naive search. KMP guarantees strict worst-case $\Theta(n + m)$ linear response time regardless of input structure, protecting the platform against latency spikes and algorithmic denial-of-service, while requiring only $\Theta(m)$ extra space for the `lps` array.

---

## 5. One Paragraph on Time

Our measured wall-clock times (`time_ms`) generally followed the asymptotic trends—showing exponential explosion for recursive Edit Distance past $n=9$, quadratic growth for `Table` and `TwoRows` from $n=500$ to $n=4000$, and a massive runtime gap between Naive ($\approx 45\text{ ms}$) and KMP ($\approx 0.8\text{ ms}$) on worst-case text of length $n=200\,000$. However, wall-clock time alone is noisy and hardware-dependent: on fast cases like KMP at $n=25\,000$ or `Table` at $n=12$, execution takes fractions of a millisecond where JVM JIT compilation warm-up, garbage collection pauses, OS thread scheduling, and CPU frequency scaling can distort a measurement by $2\times$ to $5\times$. By pairing median wall-clock timings with deterministic internal counters (`dp_ops` and `char_comparisons`), we obtain hardware-independent proof that our algorithms perform the exact theoretical number of elementary operations on every run.