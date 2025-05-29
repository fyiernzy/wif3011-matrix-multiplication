package com.wif3011.framework;

public class TiledThreadMatrixMultiplier extends TiledMatrixMultiplier {

    @Override
    protected int[][] safeMultiply(int[][] leftMatrix, int[][] rightMatrix) {
        final int[][] resultMatrix = new int[leftMatrixRows][rightMatrixCols];

        // Number of threads to use - you can tune this or get from available processors
        final int threadCount = Runtime.getRuntime().availableProcessors();

        // Create an array of threads
        Thread[] threads = new Thread[threadCount];

        // Total tiles = rowTileCount * colTileCount
        final int totalTiles = rowTileCount * colTileCount;

        // Each thread will process approximately tilesPerThread tiles
        final int tilesPerThread = (totalTiles + threadCount - 1) / threadCount; // ceil div

        for (int t = 0; t < threadCount; t++) {
            final int threadIndex = t;

            threads[t] = new Thread(() -> {
                // Calculate the tile range this thread will process
                int startTile = threadIndex * tilesPerThread;
                int endTile = Math.min(startTile + tilesPerThread, totalTiles);

                for (int tileIndex = startTile; tileIndex < endTile; tileIndex++) {
                    int tileRow = tileIndex / colTileCount;
                    int tileCol = tileIndex % colTileCount;

                    int tileRowStart = tileRow << BLOCK_SIZE_LOG2;
                    int tileRowEnd = Math.min(tileRowStart + ROW_BLOCK, leftMatrixRows);

                    int tileColStart = tileCol << BLOCK_SIZE_LOG2;
                    int tileColEnd = Math.min(tileColStart + COL_BLOCK, rightMatrixCols);

                    multiplyTile(
                            leftMatrix, rightMatrix, resultMatrix,
                            tileRowStart, tileRowEnd,
                            tileColStart, tileColEnd,
                            sharedDimension);
                }
            });

            threads[t].start();
        }

        // Wait for all threads to finish
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return resultMatrix;
    }
}
