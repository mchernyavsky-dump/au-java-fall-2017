package ru.spbau;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.fail;

public abstract class AbstractConcurrentLazyTest {
    public static final int TOTAL_RUNS = 500;

    @Test
    public void runTestsBatch() throws Exception {
        int[] resultsHist = new int[5000];
        for (int i = 0; i < TOTAL_RUNS; i++) {
            resultsHist[runSingleTest()]++;
        }

        String resultsString = renderResults(resultsHist);
        System.out.println(resultsString);

        if (resultsHist[1] != TOTAL_RUNS) fail();
    }

    private int runSingleTest() throws Exception {
        CountingSupplier countingSupplier = new CountingSupplier();
        Lazy<Integer> lazy = LazyFactory.createConcurrentLazy(countingSupplier);

        spamLazy(lazy);

        return countingSupplier.invocationsCount;
    }

    private String renderResults(int[] resultsHist) {
        StringBuilder sb = new StringBuilder();

        int failSum = Arrays.stream(resultsHist).sum() - resultsHist[1];
        double failRatio = (double) failSum / TOTAL_RUNS * 100.0;

        sb.append("Total runs made: " + TOTAL_RUNS + "\n");
        sb.append("Fail ratio: ").append(failRatio).append("%\n");
        sb.append("Histogram of results:\n");
        for (int i = 0; i < resultsHist.length; i++) {
            if (resultsHist[i] == 0) continue;
            sb.append(i).append(": ").append(resultsHist[i]).append("\n");
        }

        return sb.toString();
    }

    abstract protected <T> void spamLazy(Lazy<T> lazy);
}
