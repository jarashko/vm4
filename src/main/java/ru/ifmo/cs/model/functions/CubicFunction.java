package ru.ifmo.cs.model.functions;

import ru.ifmo.cs.model.DataPoint;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import java.util.ArrayList;
import java.util.List;

public class CubicFunction implements FunctionApproximation {
    private List<Double> coefficients = new ArrayList<>();
    private List<Double> calculatedValues = new ArrayList<>();
    private List<Double> errors = new ArrayList<>();
    private double standardDeviation;
    private double rSquared;

    @Override
    public FunctionApproximation approximate(List<DataPoint> points) {
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (DataPoint p : points) {
            obs.add(p.getX(), p.getY());
        }

        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(3);
        double[] coeffs = fitter.fit(obs.toList());

        coefficients.clear();
        coefficients.add(coeffs[0]);
        coefficients.add(coeffs[1]);
        coefficients.add(coeffs[2]);
        coefficients.add(coeffs[3]);

        calculateMetrics(points);
        return this;
    }

    @Override
    public String getName() {
        return "Кубическая";
    }

    @Override
    public List<Double> getCoefficients() {
        return coefficients;
    }

    @Override
    public double calculate(double x) {
        return coefficients.get(0) +
                coefficients.get(1) * x +
                coefficients.get(2) * x * x +
                coefficients.get(3) * x * x * x;
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

    private void calculateMetrics(List<DataPoint> points) {
        calculatedValues.clear();
        errors.clear();

        double sse = 0;
        double sst = 0;
        double meanY = points.stream().mapToDouble(DataPoint::getY).average().orElse(0);

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
        rSquared = 1 - sse / sst;
    }
}
