package ru.ifmo.cs.model.functions;

import ru.ifmo.cs.model.DataPoint;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import java.util.ArrayList;
import java.util.List;

public class LogarithmicFunction implements FunctionApproximation {
    private List<Double> coefficients = new ArrayList<>();
    private List<Double> calculatedValues = new ArrayList<>();
    private List<Double> errors = new ArrayList<>();
    private double standardDeviation;
    private double rSquared;

    @Override
    public FunctionApproximation approximate(List<DataPoint> points) {
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (DataPoint p : points) {
            if (p.getX() <= 0) throw new IllegalArgumentException("X must be positive for logarithmic function");
            obs.add(Math.log(p.getX()), p.getY());
        }

        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(1);
        double[] coeffs = fitter.fit(obs.toList());

        coefficients.clear();
        coefficients.add(coeffs[0]);
        coefficients.add(coeffs[1]);

        calculateMetrics(points);
        return this;
    }

    @Override
    public String getName() {
        return "Логарифмическая";
    }

    @Override
    public List<Double> getCoefficients() {
        return coefficients;
    }

    @Override
    public double calculate(double x) {
        if (coefficients == null || coefficients.size() < 2) {
            System.err.println("LogarithmicFunction: Коэффициенты не инициализированы или их недостаточно для вычисления.");
            return Double.NaN;
        }
        if (x <= 1e-9) {
            return Double.NaN;
        }
        try {
            return coefficients.get(0) + coefficients.get(1) * Math.log(x);
        } catch (IndexOutOfBoundsException e) {
            System.err.println("LogarithmicFunction: Ошибка доступа к коэффициентам: " + e.getMessage());
            return Double.NaN;
        }
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
