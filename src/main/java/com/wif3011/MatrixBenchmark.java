package com.wif3011;

import com.wif3011.framework.MatrixMultiplier;
import com.wif3011.framework.SequentialMatrixMultiplier;
import com.wif3011.framework.TiledExecServiceMatrixMultiplier;
import com.wif3011.framework.TiledForkJoinMatrixMultiplier;
import com.wif3011.framework.TiledParallelMatrixMultiplier;
import com.wif3011.framework.TiledThreadMatrixMultiplier;
import com.wif3011.util.MatrixUtil;

import java.util.Arrays;

public class MatrixBenchmark {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java MatrixBenchmark <impl-name> <matrix-size>");
            return;
        }

        String impl = args[0];
        int size = Integer.parseInt(args[1]);

        MatrixMultiplier calculator;

        switch (impl.toLowerCase()) {
            case "seq":
                calculator = new SequentialMatrixMultiplier();
                break;
            case "par":
                calculator = new TiledParallelMatrixMultiplier();
                break;
            case "forkjoin":
                calculator = new TiledForkJoinMatrixMultiplier();
                break;
            case "exec":
                calculator = new TiledExecServiceMatrixMultiplier();
                break;
            case "thread":
                calculator = new TiledThreadMatrixMultiplier();
                break;
            default:
                System.out.println("Unknown implementation.");
                return;
        }

        System.out.println("Running " + calculator.getClass().getSimpleName() + " on size " + size + "x" + size);
        Runtime runtime = Runtime.getRuntime();
        System.out.println("====================================================================================");
        System.out.printf("| %-35s | %-10s | %-10s | %-15s | %-8s |\n",
                "Implementation", "MatrixSize", "Time(ms)", "MemoryUsed(KB)", "Correct");
        System.out.println("------------------------------------------------------------------------------------");

        long start = System.currentTimeMillis();
        final int[][] matrixA = MatrixUtil.generate(size, size, 0, 10);
        final int[][] matrixB = MatrixUtil.generate(size, size, 0, 10);
        System.out.println(
                "Matrices of size " + size + " generated in " + (System.currentTimeMillis() - start) + "ms");

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
        for (int i = 0; i < 10; i++) {
            calculator.multiply(matrixA, matrixB);
        }

        System.gc(); // Suggest GC before measuring
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        start = System.currentTimeMillis();

        int[][] result = calculator.multiply(matrixA, matrixB);

        long timeMs = System.currentTimeMillis() - start;
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();

        long memoryUsedKb = (memoryAfter - memoryBefore) / 1024;
        boolean correct = Arrays.deepEquals(matrixC, result);

        System.out.printf("| %-35s | %-10d | %-10d | %-15d | %-8s |\n",
                calculator.getClass().getSimpleName(),
                size,
                timeMs,
                memoryUsedKb,
                correct ? "Passed" : "Failed");

    }
}
