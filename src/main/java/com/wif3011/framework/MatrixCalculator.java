package com.wif3011.framework;

public abstract class MatrixCalculator {
    public final int[][] multiply(int[][] matrixA, int[][] matrixB) {
        checkMetrics(matrixA, matrixB);
        return safeMultiply(matrixA, matrixB);
    }

    protected abstract int[][] safeMultiply(int[][] matrixA, int[][] matrixB);

    private void checkMetrics(int[][] matrixA, int[][] matrixB) {
        // 1. Check if the matrices are null or empty
        if (matrixA == null || matrixB == null || matrixA.length == 0 || matrixB.length == 0) {
            throw new IllegalArgumentException("Matrices cannot be null or empty");
        }

        // 2. Check if matrices have some columns numbers
        int colNumA = matrixA[0].length;
        for (int i = 1; i < matrixA.length; i++) {
            if (matrixA[i] == null || matrixA[i].length != colNumA) {
                throw new IllegalArgumentException("Matrix A has inconsistent column sizes");
            }
        }

        int colNumB = matrixB[0].length;
        for (int i = 1; i < matrixB.length; i++) {
            if (matrixB[i] == null || matrixB[i].length != colNumB) {
                throw new IllegalArgumentException("Matrix B has inconsistent column sizes");
            }
        }

        // 3. Check if the number of columns in matrix A is equal to the number of rows in matrix B
        if (colNumA != matrixB.length) {
            throw new IllegalArgumentException("Number of columns in Matrix A must be equal to number of rows in Matrix B");
        }
    }
}
