package ru.ifmo.cs.model.functions;

import ru.ifmo.cs.model.DataPoint;

import java.util.List;

public interface FunctionApproximation {
    FunctionApproximation approximate(List<DataPoint> points);

    String getName();
    List<Double> getCoefficients();
    double calculate(double x);
    double getStandardDeviation();
    double getRSquared();
    List<Double> getCalculatedValues();
    List<Double> getErrors();

    default String getCoefficientsAsString() {
        List<Double> coeffs = getCoefficients();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < coeffs.size(); i++) {
            sb.append(String.format("%.6f", coeffs.get(i)));
            if (i < coeffs.size() - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}
