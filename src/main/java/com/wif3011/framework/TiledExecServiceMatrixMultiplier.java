package com.wif3011.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Matrix multiplier using ExecutorService with tiled multiplication.
 */
public class TiledExecServiceMatrixMultiplier extends TiledMatrixMultiplier {

    @Override
    protected int[][] safeMultiply(int[][] leftMatrix, int[][] rightMatrix) {
        final int[][] resultMatrix = new int[leftMatrixRows][rightMatrixCols];

        final List<Callable<Void>> tileTasks = new ArrayList<>(rowTileCount * colTileCount);

        for (int tileRow = 0; tileRow < rowTileCount; tileRow++) {
            // tileRow << BLOCK_SIZE_LOG2 is equivalent to tileRow * BLOCK_SIZE
            final int tileRowStart = tileRow << BLOCK_SIZE_LOG2;
            // Math.min(tileRowStart + ROW_BLOCK, leftMatrixRows) ensures we don't go out of bounds
            final int tileRowEnd = Math.min(tileRowStart + ROW_BLOCK, leftMatrixRows);

            for (int tileCol = 0; tileCol < colTileCount; tileCol++) {
                // tileCol << BLOCK_SIZE_LOG2 is equivalent to tileCol * BLOCK_SIZE
                final int tileColStart = tileCol << BLOCK_SIZE_LOG2;
                // Math.min(tileColStart + ROW_BLOCK, rightMatrixCols) ensures we don't go out of bounds
                final int tileColEnd = Math.min(tileColStart + COL_BLOCK, rightMatrixCols);

                tileTasks.add(() -> {
                    multiplyTile(
                        leftMatrix, rightMatrix, resultMatrix,
                        tileRowStart, tileRowEnd,
                        tileColStart, tileColEnd,
                        sharedDimension
                    );
                    return null;
                });
            }
        }

        try (ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            executor.invokeAll(tileTasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return resultMatrix;
    }
}
