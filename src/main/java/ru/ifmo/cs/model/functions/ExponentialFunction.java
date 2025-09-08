package ru.ifmo.cs.model.functions;

import ru.ifmo.cs.model.DataPoint;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import java.util.ArrayList;
import java.util.List;

public class ExponentialFunction implements FunctionApproximation {
    private List<Double> coefficients = new ArrayList<>();
    private List<Double> calculatedValues = new ArrayList<>(); // Будут храниться предсказания для ВСЕХ исходных точек
    private List<Double> errors = new ArrayList<>();           // Будут храниться ошибки для ВСЕХ исходных точек
    private double standardDeviation;
    private double rSquared;

    @Override
    public FunctionApproximation approximate(List<DataPoint> allOriginalPoints) {
        WeightedObservedPoints obs = new WeightedObservedPoints();
        // Список pointsUsedForFitting не нужен напрямую здесь, так как мы используем allOriginalPoints
        // для calculateMetricsAndPopulateLists, но obs будет содержать только валидные точки для фиттинга.

        // Отчистка предыдущих результатов
        coefficients.clear();
        calculatedValues.clear();
        errors.clear();
        standardDeviation = Double.NaN;
        rSquared = Double.NaN;

        // Фильтруем точки: для экспоненциальной аппроксимации y должен быть > 0
        for (DataPoint p : allOriginalPoints) {
            if (p.getY() > 0) {
                obs.add(p.getX(), Math.log(p.getY()));
            }
        }

        // Проверяем, достаточно ли точек для аппроксимации (минимум 2 для линейной регрессии)
        if (obs.toList().size() < 2) {
            // Если точек недостаточно, устанавливаем коэффициенты в NaN, сигнализируя о неудаче.
            coefficients.add(Double.NaN); // Коэффициент 'a'
            coefficients.add(Double.NaN); // Коэффициент 'b'

            // Заполняем calculatedValues и errors значениями NaN для всех исходных точек
            // чтобы их размеры совпадали с dataPoints.
            for (int i = 0; i < allOriginalPoints.size(); i++) {
                calculatedValues.add(Double.NaN);
                errors.add(Double.NaN);
            }
            return this; // Возвращаем текущий объект в состоянии "не вычислено"
        }

        // Выполняем линейную аппроксимацию для ln(y) = ln(a) + b*x
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(1); // Степень 1 для линейной регрессии
        double[] fittedLogCoeffs = fitter.fit(obs.toList()); // coeffs[0] = ln(a), coeffs[1] = b

        // Сохраняем восстановленные коэффициенты (a и b)
        coefficients.add(Math.exp(fittedLogCoeffs[0])); // a = exp(coeffs[0])
        coefficients.add(fittedLogCoeffs[1]);           // b = coeffs[1]

        // Вычисляем метрики и заполняем списки для всех исходных точек
        calculateMetricsAndPopulateLists(allOriginalPoints);

        return this;
    }

    @Override
    public String getName() {
        return "Экспоненциальная";
    }

    @Override
    public List<Double> getCoefficients() {
        return coefficients;
    }

    @Override
    public double calculate(double x) {
        // Если коэффициенты невалидны (например, аппроксимация не удалась), возвращаем NaN
        if (coefficients.isEmpty() || coefficients.get(0).isNaN() || coefficients.get(1).isNaN()) {
            return Double.NaN;
        }
        // Вычисляем значение функции ae^(bx)
        return coefficients.get(0) * Math.exp(coefficients.get(1) * x);
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

    private void calculateMetricsAndPopulateLists(List<DataPoint> allOriginalPoints) {
        calculatedValues.clear();
        errors.clear();

        double sse = 0;
        double sst = 0;

        if (allOriginalPoints.isEmpty()) {
            standardDeviation = Double.NaN;
            rSquared = Double.NaN;
            return;
        }

        double meanY = allOriginalPoints.stream().mapToDouble(DataPoint::getY).average().orElse(0);

        int validPredictionCount = 0;
        for (DataPoint p : allOriginalPoints) {
            double yActual = p.getY();
            double yPredicted = calculate(p.getX());

            calculatedValues.add(yPredicted);

            if (!Double.isNaN(yPredicted) && Double.isFinite(yPredicted)) {
                double error = yPredicted - yActual;
                errors.add(error);
                sse += error * error;
                validPredictionCount++;
            } else {
                errors.add(Double.NaN);
            }

            sst += (yActual - meanY) * (yActual - meanY);
        }

        if (validPredictionCount > 0) {
            standardDeviation = Math.sqrt(sse / validPredictionCount);
        } else {
            standardDeviation = Double.NaN;
        }

        if (sst > 0) {
            rSquared = 1 - sse / sst;
        } else {
            rSquared = (sse == 0) ? 1.0 : Double.NaN;
        }
    }
}