package com.wif3011;

import com.wif3011.framework.MatrixCalculator;
import com.wif3011.framework.TiledExecServiceMatrixCalculator;
import com.wif3011.framework.TiledForkJoinMatrixCalculator;
import com.wif3011.framework.TiledParallelMatrixCalculator;
import com.wif3011.util.MatrixUtil;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        final int[][] matrixA = MatrixUtil.generate(5_000, 5_000, 0, 1000);
        final int[][] matrixB = MatrixUtil.generate(5_000, 5_000, 0, 1000);
        System.out.println("Matrices generated in " + (System.currentTimeMillis() - start) + "ms");

//        start = System.currentTimeMillis();
//        int[][] matrixC = new int[matrixA.length][matrixB[0].length];
//        for (int i = 0; i < matrixA.length; i++) {
//            for (int j = 0; j < matrixA[i].length; j++) {
//                int sum = 0;
//                for (int k = 0; k < matrixB.length; k++) {
//                    sum += matrixA[i][k] * matrixB[k][j];
//                }
//                matrixC[i][j] = sum;
//            }
//        }
//        System.out.println("Multiplication C completed in " + (System.currentTimeMillis() - start) + "ms");

        MatrixCalculator[] calculators = {
            new TiledParallelMatrixCalculator(),
            new TiledForkJoinMatrixCalculator(),
            new TiledExecServiceMatrixCalculator()
        };

        for (MatrixCalculator calculator : calculators) {
            start = System.currentTimeMillis();
            int[][] matrix = calculator.multiply(matrixA, matrixB);
            System.out.println("Multiplication " + calculator.getClass().getSimpleName() + " completed in " + (System.currentTimeMillis() - start) + "ms");
            // Verify the result using Arrays.deepEquals
//            System.out.println("Result verification: " + (Arrays.deepEquals(matrixC, matrix) ? "Passed" : "Failed"));
        }
    }
}