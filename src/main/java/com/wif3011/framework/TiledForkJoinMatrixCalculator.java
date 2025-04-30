package com.wif3011.framework;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * 2D-tiled fork/join matrix multiplier that splits work along both row and column dimensions.
 * Uses power-of-two block sizes and bit-shift operations for thresholding and midpoints.
 */
public class TiledForkJoinMatrixCalculator extends MatrixCalculator {
    private static final int ROW_SHIFT = 6;                // log2(64)
    private static final int COL_SHIFT = 6;                // log2(64)
    private static final int ROW_BLOCK = 1 << ROW_SHIFT;   // 64 rows per block
    private static final int COL_BLOCK = 1 << COL_SHIFT;   // 64 cols per block

    @Override
    public int[][] safeMultiply(int[][] A, int[][] B) {
        int rowsA = A.length;
        int colsA = A[0].length;
        int colsB = B[0].length;
        int[][] C = new int[rowsA][colsB];

        // Invoke a single root task on the common pool
        try (ForkJoinPool pool = ForkJoinPool.commonPool()) {
            pool.submit(new MultiplyTask(A, B, C,
                /*startRow=*/0,        /*endRow=*/rowsA,
                /*startCol=*/0,        /*endCol=*/colsB,
                /*innerDim=*/colsA)).get();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        return C;
    }

    /**
     * RecursiveAction that splits a submatrix [startRow..endRow)×[startCol..endCol)
     * either along rows or columns (whichever span is larger) until both dims are within block limits.
     */
    private static class MultiplyTask extends RecursiveAction {
        final int[][] A, B, C;
        final int startRow, endRow, startCol, endCol, innerDim;

        MultiplyTask(int[][] A, int[][] B, int[][] C,
                     int startRow, int endRow,
                     int startCol, int endCol,
                     int innerDim) {
            this.A = A;
            this.B = B;
            this.C = C;
            this.startRow = startRow;
            this.endRow = endRow;
            this.startCol = startCol;
            this.endCol = endCol;
            this.innerDim = innerDim;
        }

        @Override
        protected void compute() {
            int rowCount = endRow - startRow;
            int colCount = endCol - startCol;

            // Base case: submatrix fits within a single block
            if (rowCount <= ROW_BLOCK && colCount <= COL_BLOCK) {
                for (int i = startRow; i < endRow; i++) {
                    int[] aRow = A[i];
                    int[] cRow = C[i];
                    for (int k = 0; k < innerDim; k++) {
                        int aVal = aRow[k];
                        int[] bRow = B[k];
                        for (int j = startCol; j < endCol; j++) {
                            cRow[j] += aVal * bRow[j];
                        }
                    }
                }
            }
            // Otherwise split along the larger dimension
            else if (rowCount >= colCount) {
                int midRow = (startRow + endRow) >>> 1;  // bit-shift divide by 2
                invokeAll(
                    new MultiplyTask(A, B, C, startRow, midRow, startCol, endCol, innerDim),
                    new MultiplyTask(A, B, C, midRow, endRow, startCol, endCol, innerDim)
                );
            } else {
                int midCol = (startCol + endCol) >>> 1;
                invokeAll(
                    new MultiplyTask(A, B, C, startRow, endRow, startCol, midCol, innerDim),
                    new MultiplyTask(A, B, C, startRow, endRow, midCol, endCol, innerDim)
                );
            }
        }
    }
}
