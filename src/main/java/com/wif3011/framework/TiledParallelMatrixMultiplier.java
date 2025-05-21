package com.wif3011.framework;

import java.util.stream.IntStream;

public class TiledParallelMatrixMultiplier extends TiledMatrixMultiplier {

    @Override
    protected int[][] safeMultiply(int[][] leftMatrix, int[][] rightMatrix) {
        final int[][] resultMatrix = new int[leftMatrixRows][rightMatrixCols];

        IntStream
            .iterate(0, i -> i + 1)
            .limit(rowTileCount)
            .parallel()
            .forEach(tileRow -> {
                // tileRow << BLOCK_SIZE_LOG2 is equivalent to tileRow * BLOCK_SIZE
                final int tileRowStart = tileRow << BLOCK_SIZE_LOG2;
                // Math.min(tileRowStart + ROW_BLOCK, leftMatrixRows) ensures we don't go out of bounds
                final int tileRowEnd = Math.min(tileRowStart + ROW_BLOCK, leftMatrixRows);

                for (int tileCol = 0; tileCol < colTileCount; tileCol++) {
                    // tileCol << BLOCK_SIZE_LOG2 is equivalent to tileCol * BLOCK_SIZE
                    final int tileColStart = tileCol << BLOCK_SIZE_LOG2;
                    // Math.min(tileColStart + ROW_BLOCK, rightMatrixCols) ensures we don't go out of bounds
                    final int tileColEnd = Math.min(tileColStart + COL_BLOCK, rightMatrixCols);

                    multiplyTile(
                        leftMatrix, rightMatrix, resultMatrix,
                        tileRowStart, tileRowEnd,
                        tileColStart, tileColEnd,
                        sharedDimension
                    );
                }
            });

        return resultMatrix;
    }
}
