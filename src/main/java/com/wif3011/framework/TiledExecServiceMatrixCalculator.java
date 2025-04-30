package com.wif3011.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * MatrixCalculator implementation using ExecutorService with try-with-resources (AutoCloseable).
 * Divides the result into 64×64 blocks (power-of-two), leveraging bit-shifts for indices,
 * and executes each block in parallel on a fixed thread pool.
 */
public class TiledExecServiceMatrixCalculator extends MatrixCalculator {
    private static final int ROW_SHIFT = 6;            // log2(64)
    private static final int COL_SHIFT = 6;
    private static final int ROW_BLOCK = 1 << ROW_SHIFT;
    private static final int COL_BLOCK = 1 << COL_SHIFT;

    @Override
    protected int[][] safeMultiply(int[][] A, int[][] B) {
        int rowsA = A.length;
        int colsA = A[0].length;
        int colsB = B[0].length;
        int[][] C = new int[rowsA][colsB];

        // Calculate number of blocks using bit-shifts (ceil division)
        int rowBlocks = (rowsA + ROW_BLOCK - 1) >>> ROW_SHIFT;
        int colBlocks = (colsB + COL_BLOCK - 1) >>> COL_SHIFT;

        int nThreads = Runtime.getRuntime().availableProcessors();
        // ExecutorService implements AutoCloseable in Java 21+, so use try-with-resources
        try (ExecutorService executor = Executors.newFixedThreadPool(nThreads)) {
            List<Future<?>> futures = new ArrayList<>();

            // Submit one task per block
            for (int bi = 0; bi < rowBlocks; bi++) {
                final int i0 = bi << ROW_SHIFT;
                final int iMax = Math.min(i0 + ROW_BLOCK, rowsA);
                for (int bj = 0; bj < colBlocks; bj++) {
                    final int j0 = bj << COL_SHIFT;
                    final int jMax = Math.min(j0 + COL_BLOCK, colsB);

                    futures.add(executor.submit(() -> {
                        for (int i = i0; i < iMax; i++) {
                            int[] aRow = A[i];
                            int[] cRow = C[i];
                            for (int k = 0; k < colsA; k++) {
                                int aVal = aRow[k];
                                int[] bRow = B[k];
                                for (int j = j0; j < jMax; j++) {
                                    cRow[j] += aVal * bRow[j];
                                }
                            }
                        }
                    }));
                }
            }

            // Wait for all submitted tasks to complete
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    throw new RuntimeException("Block computation failed", e.getCause());
                }
            }
        }

        return C;
    }
}

