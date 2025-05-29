package com.wif3011;

import com.wif3011.framework.MatrixMultiplier;
import com.wif3011.framework.SequentialMatrixMultiplier;
import com.wif3011.framework.TiledExecServiceMatrixMultiplier;
import com.wif3011.framework.TiledForkJoinMatrixMultiplier;
import com.wif3011.framework.TiledParallelMatrixMultiplier;
import com.wif3011.framework.TiledThreadMatrixMultiplier;
import com.wif3011.util.MatrixUtil;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        final int[][] matrixA = MatrixUtil.generate(1_500, 1_000, 0, 10);
        final int[][] matrixB = MatrixUtil.generate(1_000, 1_500, 0, 10);
        System.out.println("Matrices generated in " + (System.currentTimeMillis() - start) + "ms");

        start = System.currentTimeMillis();
        int[][] matrixC = new int[matrixA.length][matrixB[0].length];
        for (int row = 0; row < matrixA.length; row++) {
            for (int col = 0; col < matrixC[row].length; col++) {
                int sum = 0;
                for (int k = 0; k < matrixB.length; k++) {
                    sum += matrixA[row][k] * matrixB[k][col];
                }
                matrixC[row][col] = sum;
            }
        }

        System.out.println("Multiplication C completed in " + (System.currentTimeMillis() - start) + "ms");

        MatrixMultiplier[] calculators = {
                new SequentialMatrixMultiplier(),
                new TiledParallelMatrixMultiplier(),
                new TiledForkJoinMatrixMultiplier(),
                new TiledExecServiceMatrixMultiplier(),
                new TiledThreadMatrixMultiplier()
        };

        for (MatrixMultiplier calculator : calculators) {
            // Warm up the JIT
            for (int i = 0; i < 10; i++) {
                calculator.multiply(matrixA, matrixB);
            }
            start = System.currentTimeMillis();
            int[][] matrix = calculator.multiply(matrixA, matrixB);
            System.out.println("Multiplication " + calculator.getClass().getSimpleName() + " completed in "
                    + (System.currentTimeMillis() - start) + "ms");
            // Verify the result using Arrays.deepEquals
            System.out.println("Result verification: " + (Arrays.deepEquals(matrixC, matrix) ? "Passed" : "Failed"));
        }
    }
}