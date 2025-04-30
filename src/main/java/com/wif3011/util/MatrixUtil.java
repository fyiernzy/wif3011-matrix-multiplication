package com.wif3011.util;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ThreadLocalRandom;

public class MatrixUtil {
    public static void print(int[][] matrix) {
        for (int[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }


    /**
     * Generates a rowNum×colNum matrix of random ints in [minValue…maxValue],
     * using a work‐stealing ForkJoin task that splits only down to tileSize rows per leaf.
     *
     * @param rowNum   number of rows
     * @param colNum   number of columns
     * @param minValue inclusive minimum random value
     * @param maxValue inclusive maximum random value
     * @return filled int[rowNum][colNum] matrix
     */
    public static int[][] generate(int rowNum, int colNum, int minValue, int maxValue) {
        int[][] matrix = new int[rowNum][colNum];
        int range = maxValue - minValue + 1;

        // Determine chunk size: roughly rows-per-CPU, but no larger than tileSize
        int cpus = Runtime.getRuntime().availableProcessors();
        int rowsPerCpu = (rowNum + cpus - 1) / cpus;
        int tileSize = 128;  // you can tune this (e.g. 64–512) for your cache
        int threshold = Math.min(tileSize, rowsPerCpu);

        // Submit the single root task to the common ForkJoinPool
        try (ForkJoinPool pool = ForkJoinPool.commonPool()) {
            pool.submit(new FillTask(matrix, 0, rowNum, colNum, minValue, range, threshold)).get();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }

        return matrix;
    }

    /**
     * RecursiveAction that splits work into row‐chunks until span ≤ threshold,
     * then fills each row with two random ints per nextLong() call.
     */
    private static class FillTask extends RecursiveAction {
        private final int[][] matrix;
        private final int start, end, colNum, minValue, range, threshold;

        FillTask(int[][] matrix,
                 int start, int end,
                 int colNum, int minValue,
                 int range, int threshold) {
            this.matrix = matrix;
            this.start = start;
            this.end = end;
            this.colNum = colNum;
            this.minValue = minValue;
            this.range = range;
            this.threshold = threshold;
        }

        @Override
        protected void compute() {
            int span = end - start;
            if (span <= threshold) {
                // Leaf: fill rows [start, end)
                ThreadLocalRandom rnd = ThreadLocalRandom.current();
                for (int r = start; r < end; r++) {
                    int[] row = matrix[r];
                    int c = 0, len = colNum;

                    // Generate two ints per nextLong() call
                    while (c + 1 < len) {
                        long bits = rnd.nextLong();
                        // low 32 bits
                        row[c++] = (int) (bits & 0xFFFFFFFFL) % range + minValue;
                        // high 32 bits
                        row[c++] = (int) ((bits >>> 32) & 0xFFFFFFFFL) % range + minValue;
                    }
                    // If odd number of columns, generate one more
                    if (c < len) {
                        row[c] = rnd.nextInt(range) + minValue;
                    }
                }
            } else {
                // Split into two subtasks
                int mid = (start + end) >>> 1;
                invokeAll(
                    new FillTask(matrix, start, mid, colNum, minValue, range, threshold),
                    new FillTask(matrix, mid, end, colNum, minValue, range, threshold)
                );
            }
        }
    }
}


