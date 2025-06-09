package ru.ifmo.cs.model.functions;

import ru.ifmo.cs.model.DataPoint;
import ru.ifmo.cs.service.MathService;
import java.util.ArrayList;
import java.util.List;

public class QuadraticFunction implements FunctionApproximation {
    private List<Double> coefficients = new ArrayList<>();
    private List<Double> calculatedValues = new ArrayList<>();
    private List<Double> errors = new ArrayList<>();
    private double standardDeviation;
    private double rSquared;

    @Override
    public FunctionApproximation approximate(List<DataPoint> points) {
        double[] coeffs = MathService.polynomialRegression(points, 2);

        coefficients.clear();
        coefficients.add(coeffs[0]); 
        coefficients.add(coeffs[1]); 
        coefficients.add(coeffs[2]); 

        calculateMetrics(points);

        return this;
    }

    @Override
    public String getName() {
        return "Квадратичная";
    }

    @Override
    public List<Double> getCoefficients() {
        return coefficients;
    }

    @Override
    public double calculate(double x) {
        // y = ax² + bx + c
        return coefficients.get(0) +
                coefficients.get(1) * x +
                coefficients.get(2) * x * x;
    }

    @Override
    public double getStandardDeviation() {
        return standardDeviation;
    }

    @Override
    public double getRSquared() {
        return rSquared;
    }

    @Override
    public List<Double> getCalculatedValues() {
        return calculatedValues;
    }

    @Override
    public List<Double> getErrors() {
        return errors;
    }

    @Override
    public String getCoefficientsAsString() {
        return String.format("y = %.4f + %.4f·x + %.4f·x²",
                coefficients.get(0),
                coefficients.get(1),
                coefficients.get(2));
    }

    private void calculateMetrics(List<DataPoint> points) {
        calculatedValues.clear();
        errors.clear();

        double sse = 0; 
        double sst = 0; 
        double meanY = calculateMeanY(points);

        for (DataPoint p : points) {
            double yActual = p.getY();
            double yPredicted = calculate(p.getX());

            calculatedValues.add(yPredicted);
            double error = yPredicted - yActual;
            errors.add(error);

            sse += error * error;
            sst += (yActual - meanY) * (yActual - meanY);
        }

        standardDeviation = Math.sqrt(sse / points.size());

        rSquared = 1 - (sse / sst);
    }

    private double calculateMeanY(List<DataPoint> points) {
        return points.stream()
                .mapToDouble(DataPoint::getY)
                .average()
                .orElse(0.0);
    }
}