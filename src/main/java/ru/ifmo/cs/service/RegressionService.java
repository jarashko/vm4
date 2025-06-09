package ru.ifmo.cs.service;

import ru.ifmo.cs.model.DataPoint;
import ru.ifmo.cs.model.functions.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RegressionService {
    private RegressionResult lastResult;
    private static final double EPSILON = 1e-10;

    public RegressionResult calculateAll(List<DataPoint> points) {
        RegressionResult result = new RegressionResult();

        List<FunctionApproximation> functions = new ArrayList<>();
        functions.add(new LinearFunction().approximate(points));
        functions.add(new QuadraticFunction().approximate(points));
        functions.add(new CubicFunction().approximate(points));
        functions.add(new ExponentialFunction().approximate(points));
        functions.add(new LogarithmicFunction().approximate(points));
        functions.add(new PowerFunction().approximate(points));

        double pearson = calculatePearsonCorrelation(points);

        FunctionApproximation bestFunction = functions.stream()
                .min(Comparator.comparingDouble(FunctionApproximation::getStandardDeviation))
                .orElseThrow();

        result.setAllFunctions(functions);
        result.setBestFunction(bestFunction);
        result.setPearsonCorrelation(pearson);

        lastResult = result;
        return result;
    }

    private double calculatePearsonCorrelation(List<DataPoint> points) {
        int n = points.size();
        double sumX = 0, sumY = 0, sumXY = 0;
        double sumX2 = 0, sumY2 = 0;

        for (DataPoint p : points) {
            double x = p.getX();
            double y = p.getY();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
            sumY2 += y * y;
        }

        double numerator = n * sumXY - sumX * sumY;
        double denominator = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));

        return Math.abs(denominator) < EPSILON ? 0 : numerator / denominator;
    }

    public RegressionResult getLastResult() {
        return lastResult;
    }
}
