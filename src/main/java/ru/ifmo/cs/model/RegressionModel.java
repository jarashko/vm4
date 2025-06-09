package ru.ifmo.cs.model;

import java.util.ArrayList;
import java.util.List;

public abstract class RegressionModel {
    protected List<Double> coefficients;
    protected double standardDeviation;
    protected double rSquared;
    protected List<Double> calculatedValues;
    protected List<Double> errors;

    public abstract String getName();
    public abstract void calculate(List<DataPoint> dataPoints);

    public List<Double> getCoefficients() {
        return coefficients;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public double getRSquared() {
        return rSquared;
    }

    public List<Double> getCalculatedValues() {
        return calculatedValues;
    }

    public List<Double> getErrors() {
        return errors;
    }

    public String getCoefficientsAsString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < coefficients.size(); i++) {
            sb.append(String.format("%.6f", coefficients.get(i)));
            if (i < coefficients.size() - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    protected void calculateMetrics(List<DataPoint> dataPoints) {
        double sse = 0.0;
        double sst = 0.0;
        double meanY = calculateMeanY(dataPoints);

        calculatedValues = new ArrayList<>();
        errors = new ArrayList<>();

        for (DataPoint point : dataPoints) {
            double predicted = calculate(point.getX());
            double actual = point.getY();
            double error = predicted - actual;

            calculatedValues.add(predicted);
            errors.add(error);
            sse += error * error;
            sst += Math.pow(actual - meanY, 2);
        }

        standardDeviation = Math.sqrt(sse / dataPoints.size());
        rSquared = 1.0 - (sse / sst);
    }

    protected double calculateMeanY(List<DataPoint> dataPoints) {
        return dataPoints.stream()
                .mapToDouble(DataPoint::getY)
                .average()
                .orElse(0.0);
    }

    public double calculate(double x) {
        return 0.0;
    }
}
