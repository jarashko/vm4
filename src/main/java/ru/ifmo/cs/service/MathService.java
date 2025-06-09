package ru.ifmo.cs.service;

import ru.ifmo.cs.model.DataPoint;
import ru.ifmo.cs.model.functions.FunctionApproximation;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.ArrayList;
import java.util.List;

public class MathService {

    public static double[] polynomialRegression(double[] xValues, double[] yValues, int degree) {
        int n = xValues.length;
        double[][] xData = new double[n][degree + 1];
        double[] yData = new double[n];

        for (int i = 0; i < n; i++) {
            double x = xValues[i];
            yData[i] = yValues[i];

            for (int j = 0; j <= degree; j++) {
                xData[i][j] = Math.pow(x, j);
            }
        }

        RealMatrix X = new Array2DRowRealMatrix(xData, false);
        RealMatrix XT = X.transpose();
        RealMatrix XTX = XT.multiply(X);

        RealVector Y = new ArrayRealVector(yData, false);
        RealVector XTY = XT.operate(Y);

        DecompositionSolver solver = new LUDecomposition(XTX).getSolver();
        return solver.solve(XTY).toArray();
    }

    public static double[] polynomialRegression(List<DataPoint> points, int degree) {
        double[] x = points.stream().mapToDouble(DataPoint::getX).toArray();
        double[] y = points.stream().mapToDouble(DataPoint::getY).toArray();
        return polynomialRegression(x, y, degree);
    }

    public static double[] exponentialRegression(List<DataPoint> points) {
        List<Double> xList = new ArrayList<>();
        List<Double> yList = new ArrayList<>();

        for (DataPoint p : points) {
            if (p.getY() <= 0) continue;
            xList.add(p.getX());
            yList.add(Math.log(p.getY()));
        }

        double[] x = xList.stream().mapToDouble(Double::doubleValue).toArray();
        double[] y = yList.stream().mapToDouble(Double::doubleValue).toArray();
        double[] linearCoefs = polynomialRegression(x, y, 1);
        return new double[]{Math.exp(linearCoefs[0]), linearCoefs[1]};
    }

    public static double[] logarithmicRegression(List<DataPoint> points) {
        List<Double> xList = new ArrayList<>();
        List<Double> yList = new ArrayList<>();

        for (DataPoint p : points) {
            if (p.getX() <= 0) continue;
            xList.add(Math.log(p.getX()));
            yList.add(p.getY());
        }

        double[] x = xList.stream().mapToDouble(Double::doubleValue).toArray();
        double[] y = yList.stream().mapToDouble(Double::doubleValue).toArray();
        return polynomialRegression(x, y, 1);
    }

    public static double[] powerRegression(List<DataPoint> points) {
        List<Double> xList = new ArrayList<>();
        List<Double> yList = new ArrayList<>();

        for (DataPoint p : points) {
            if (p.getX() <= 0 || p.getY() <= 0) continue;
            xList.add(Math.log(p.getX()));
            yList.add(Math.log(p.getY()));
        }

        double[] x = xList.stream().mapToDouble(Double::doubleValue).toArray();
        double[] y = yList.stream().mapToDouble(Double::doubleValue).toArray();
        double[] linearCoefs = polynomialRegression(x, y, 1);
        return new double[]{Math.exp(linearCoefs[0]), linearCoefs[1]};
    }

    public static double calculatePearsonCorrelation(List<DataPoint> points) {
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

        return Math.abs(denominator) < 1e-10 ? 0 : numerator / denominator;
    }

    public static double calculateStandardDeviation(FunctionApproximation function, List<DataPoint> points) {
        double sse = 0;
        for (DataPoint p : points) {
            double error = function.calculate(p.getX()) - p.getY();
            sse += error * error;
        }
        return Math.sqrt(sse / points.size());
    }

    public static double calculateRSquared(FunctionApproximation function, List<DataPoint> points) {
        double sse = 0;
        double sst = 0;
        double meanY = points.stream().mapToDouble(DataPoint::getY).average().orElse(0);

        for (DataPoint p : points) {
            double predicted = function.calculate(p.getX());
            double actual = p.getY();
            sse += Math.pow(predicted - actual, 2);
            sst += Math.pow(actual - meanY, 2);
        }

        return 1 - (sse / sst);
    }

    public static List<DataPoint> generateFunctionPoints(
            FunctionApproximation function,
            double minX,
            double maxX,
            int pointsCount
    ) {
        List<DataPoint> points = new ArrayList<>();
        double range = maxX - minX;

        for (int i = 0; i < pointsCount; i++) {
            double x = minX + i * range / (pointsCount - 1);
            double y = function.calculate(x);
            points.add(new DataPoint(x, y));
        }

        return points;
    }

    public static boolean isCollinear(List<DataPoint> points) {
        if (points.size() < 2) return true;

        double firstSlope = (points.get(1).getY() - points.get(0).getY()) /
                (points.get(1).getX() - points.get(0).getX());

        for (int i = 2; i < points.size(); i++) {
            double slope = (points.get(i).getY() - points.get(i-1).getY()) /
                    (points.get(i).getX() - points.get(i-1).getX());

            if (Math.abs(slope - firstSlope) > 1e-5) {
                return false;
            }
        }
        return true;
    }
}