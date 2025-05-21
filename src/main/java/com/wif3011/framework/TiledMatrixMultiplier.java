package com.wif3011.framework;

public abstract class TiledMatrixMultiplier extends MatrixMultiplier {
    /**
     * Size of each tile (both width and height) in elements.
     */
    protected static final int BLOCK_SIZE = 64;

    /** Log₂ of BLOCK_SIZE, used for fast shift-based multiplication/division. */
    protected static final int BLOCK_SIZE_LOG2 = 6;

    /** Number of rows in each tile (equals BLOCK_SIZE). */
    protected static final int ROW_BLOCK = BLOCK_SIZE;

    /** Number of columns in each tile (equals BLOCK_SIZE). */
    protected static final int COL_BLOCK = BLOCK_SIZE;

    /** How many row-tiles are needed to cover the entire matrix vertically. */
    protected int rowTileCount;

    /** How many column-tiles are needed to cover the entire matrix horizontally. */
    protected int colTileCount;


    protected static void multiplyTile(
        int[][] leftMatrix, int[][] rightMatrix, int[][] resultMatrix,
        int tileRowStart, int tileRowEnd,
        int tileColStart, int tileColEnd,
        int sharedDimension
    ) {
        // This multiplyTile method still calculates the value result[row][col] correctly,
        // but with a different method: it processes the data in right matrix row by row rather than column by column.
        // This is a trade-off between memory-access patterns and CPU cache usage.
        // Such a memory-access pattern offers better cache locality and potentially fewer cache misses.
        // We can, of course, use:
        //     for (int row = …) {
        //         for (int col = …) {
        //             for (int k = …) {
        //                 // compute result[row][col]
        //             }
        //         }
        //     }
        // to calculate the value, but this will be less efficient than the original method.
        for (int row = tileRowStart; row < tileRowEnd; row++) {
            // Several tests show that this is more efficient when these variables are declared here.
            // Inlining them is, in fact, equivalent to:
            //     result[row][col] = result[row][col] + leftMatrix[row][k] * rightMatrix[k][col];
            int[] resultRow = resultMatrix[row];
            for (int k = 0; k < sharedDimension; k++) {
                int leftValue = leftMatrix[row][k];
                int[] rightRow = rightMatrix[k];
                for (int col = tileColStart; col < tileColEnd; col++) {
                    resultRow[col] += leftValue * rightRow[col];
                }
            }
        }
    }

    @Override
    protected void setup(int[][] leftMatrix, int[][] rightMatrix) {
        super.setup(leftMatrix, rightMatrix);
        // Ceiling division, equivalent to Math.ceil((double) leftMatrixRows / ROW_BLOCK),
        // i.e., the number of row-tiles needed to cover the matrix vertically.
        rowTileCount = (leftMatrixRows + ROW_BLOCK - 1) / ROW_BLOCK;

        // Ceiling division, equivalent to Math.ceil((double) rightMatrixCols / COL_BLOCK),
        // i.e., the number of column-tiles needed to cover the matrix horizontally.
        colTileCount = (rightMatrixCols + COL_BLOCK - 1) / COL_BLOCK;
    }
}
