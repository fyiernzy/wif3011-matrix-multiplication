package com.wif3011.framework;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Matrix multiplier using ForkJoin framework with tiled multiplication.
 */
public class TiledForkJoinMatrixMultiplier extends TiledMatrixMultiplier {

    @Override
    protected int[][] safeMultiply(int[][] leftMatrix, int[][] rightMatrix) {
        final int[][] resultMatrix = new int[leftMatrixRows][rightMatrixCols];

        try (ForkJoinPool pool = ForkJoinPool.commonPool()) {
            pool.submit(new ForkJoinMultiplyTask(
                leftMatrix, rightMatrix, resultMatrix,
                0, leftMatrixRows,
                0, rightMatrixCols,
                sharedDimension
            )).get();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }

        return resultMatrix;
    }

    private static class ForkJoinMultiplyTask extends RecursiveAction {
        private final int[][] leftMatrix;
        private final int[][] rightMatrix;
        private final int[][] resultMatrix;

        private final int tileRowStart;
        private final int tileRowEnd;
        private final int tileColStart;
        private final int tileColEnd;

        private final int sharedDimension;

        ForkJoinMultiplyTask(
            int[][] leftMatrix, int[][] rightMatrix, int[][] resultMatrix,
            int tileRowStart, int tileRowEnd,
            int tileColStart, int tileColEnd,
            int sharedDimension) {
            this.leftMatrix = leftMatrix;
            this.rightMatrix = rightMatrix;
            this.resultMatrix = resultMatrix;
            this.tileRowStart = tileRowStart;
            this.tileRowEnd = tileRowEnd;
            this.tileColStart = tileColStart;
            this.tileColEnd = tileColEnd;
            this.sharedDimension = sharedDimension;
        }

        @Override
        protected void compute() {
            final int rowTileCount = tileRowEnd - tileRowStart;
            final int colTileCount = tileColEnd - tileColStart;

            if (rowTileCount <= ROW_BLOCK && colTileCount <= COL_BLOCK) {
                multiplyTile(
                    leftMatrix, rightMatrix, resultMatrix,
                    tileRowStart, tileRowEnd,
                    tileColStart, tileColEnd,
                    sharedDimension
                );
                return;
            }

            if (rowTileCount >= colTileCount) {
                final int midTileRow = (tileRowStart + tileRowEnd) >>> 1;
                invokeAll(
                    new ForkJoinMultiplyTask(leftMatrix, rightMatrix, resultMatrix,
                        tileRowStart, midTileRow, tileColStart, tileColEnd,
                        sharedDimension),
                    new ForkJoinMultiplyTask(leftMatrix, rightMatrix, resultMatrix,
                        midTileRow, tileRowEnd, tileColStart, tileColEnd,
                        sharedDimension)
                );
            } else {
                final int midTileCol = (tileColStart + tileColEnd) >>> 1;
                invokeAll(
                    new ForkJoinMultiplyTask(leftMatrix, rightMatrix, resultMatrix,
                        tileRowStart, tileRowEnd, tileColStart, midTileCol,
                        sharedDimension),
                    new ForkJoinMultiplyTask(leftMatrix, rightMatrix, resultMatrix,
                        tileRowStart, tileRowEnd, midTileCol, tileColEnd,
                        sharedDimension)
                );
            }
        }
    }
}
