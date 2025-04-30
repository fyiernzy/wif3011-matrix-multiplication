package com.wif3011.framework;

import java.util.stream.IntStream;

public class TiledParallelMatrixCalculator extends MatrixCalculator {
    private static final int BLOCK_SIZE = 64;

    @Override
    protected int[][] safeMultiply(int[][] matrixA, int[][] matrixB) {
        int rowsA = matrixA.length;
        int colsA = matrixA[0].length;
        int colsB = matrixB[0].length;
        int[][] result = new int[rowsA][colsB];

        IntStream
            .iterate(0, i0 -> i0 < rowsA, i0 -> i0 + BLOCK_SIZE)
            .parallel()
            .forEach(i0 -> {
                int iMax = Math.min(i0 + BLOCK_SIZE, rowsA);

                for (int j0 = 0; j0 < colsB; j0 += BLOCK_SIZE) {
                    int jMax = Math.min(j0 + BLOCK_SIZE, colsB);

                    for (int k0 = 0; k0 < colsA; k0 += BLOCK_SIZE) {
                        int kMax = Math.min(k0 + BLOCK_SIZE, colsA);

                        for (int i = i0; i < iMax; i++) {
                            int[] aRow = matrixA[i];
                            int[] cRow = result[i];

                            for (int k = k0; k < kMax; k++) {
                                int aVal = aRow[k];
                                int[] bRow = matrixB[k];
                                for (int j = j0; j < jMax; j++) {
                                    cRow[j] += aVal * bRow[j];
                                }
                            }
                        }
                    }
                }
            });

        return result;
    }
}
