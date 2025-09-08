package ru.ifmo.cs.model.functions;

import ru.ifmo.cs.model.DataPoint;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import java.util.ArrayList;
import java.util.List;

public class PowerFunction implements FunctionApproximation {
    private List<Double> coefficients = new ArrayList<>();
    private List<Double> calculatedValues = new ArrayList<>();
    private List<Double> errors = new ArrayList<>();
    private double standardDeviation;
    private double rSquared;

    @Override
    public FunctionApproximation approximate(List<DataPoint> points) {
        WeightedObservedPoints obs = new WeightedObservedPoints();
        
        // Отчистка предыдущих результатов
        coefficients.clear();
        calculatedValues.clear();
        errors.clear();
        standardDeviation = Double.NaN;
        rSquared = Double.NaN;

        // Фильтруем точки: для степенной аппроксимации x и y должны быть > 0
        for (DataPoint p : points) {
            if (p.getX() > 0 && p.getY() > 0) {
                obs.add(Math.log(p.getX()), Math.log(p.getY()));
            }
        }

        // Проверяем, достаточно ли точек для аппроксимации (минимум 2 для линейной регрессии)
        if (obs.toList().size() < 2) {
            // Если точек недостаточно, устанавливаем коэффициенты в NaN, сигнализируя о неудаче.
            coefficients.add(Double.NaN); // Коэффициент 'a'
            coefficients.add(Double.NaN); // Коэффициент 'b'

            // Заполняем calculatedValues и errors значениями NaN для всех исходных точек
            for (int i = 0; i < points.size(); i++) {
                calculatedValues.add(Double.NaN);
                errors.add(Double.NaN);
            }
            return this; // Возвращаем текущий объект в состоянии "не вычислено"
        }

        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(1);
        double[] coeffs = fitter.fit(obs.toList());

        coefficients.add(Math.exp(coeffs[0]));
        coefficients.add(coeffs[1]);

        calculateMetrics(points);
        return this;
    }

    @Override
    public String getName() {
        return "Степенная";
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
        if (x <= 0) {
            return Double.NaN;
        }
        // Вычисляем значение функции a * x^b
        return coefficients.get(0) * Math.pow(x, coefficients.get(1));
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

        double sse = 0; // Сумма квадратов ошибок только для точек, по которым сделано валидное предсказание
        double sst = 0; // Общая сумма квадратов для ВСЕХ исходных точек

        // Если исходный список пуст, метрики NaN
        if (points.isEmpty()) {
            standardDeviation = Double.NaN;
            rSquared = Double.NaN;
            return;
        }

        // Вычисляем среднее значение Y по ВСЕМ исходным точкам для R^2
        double meanY = points.stream().mapToDouble(DataPoint::getY).average().orElse(0);

        int validPredictionCount = 0; // Счетчик точек, для которых было получено валидное предсказание

        for (DataPoint p : points) {
            double yActual = p.getY();
            // yPredicted может быть NaN, если p.getX() <= 0 или p.getY() <= 0 (фильтрация для ln) или если аппроксимация не удалась
            double yPredicted = calculate(p.getX());

            calculatedValues.add(yPredicted); // Добавляем предсказание (или NaN) для каждой исходной точки

            // Ошибка и вклад в SSE/СКО учитываются только если предсказание валидно
            if (!Double.isNaN(yPredicted) && Double.isFinite(yPredicted)) {
                double error = yPredicted - yActual;
                errors.add(error);
                sse += error * error;
                validPredictionCount++;
            } else {
                errors.add(Double.NaN); // Для точек без валидного предсказания
            }

            // SST вычисляется по всем исходным значениям Y
            sst += (yActual - meanY) * (yActual - meanY);
        }

        // Вычисляем среднеквадратичное отклонение, основываясь на количестве валидных предсказаний
        if (validPredictionCount > 0) {
            standardDeviation = Math.sqrt(sse / validPredictionCount);
        } else {
            standardDeviation = Double.NaN; // Нет валидных предсказаний -> нет осмысленного СКО
        }

        // Вычисляем R^2. Если все yActual были одинаковыми (sst=0):
        //   - если sse=0 (идеальное предсказание), R^2 = 1.0.
        //   - если sse>0 (неудачное предсказание), R^2 = NaN.
        if (sst > 0) {
            rSquared = 1 - sse / sst;
        } else {
            rSquared = (sse == 0) ? 1.0 : Double.NaN;
        }
    }
}
