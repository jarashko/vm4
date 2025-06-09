package ru.ifmo.cs.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import ru.ifmo.cs.model.DataPoint;
import ru.ifmo.cs.service.RegressionService;
import ru.ifmo.cs.service.RegressionResult;
import ru.ifmo.cs.model.functions.FunctionApproximation;

public class RegressionController {

    private final RegressionService regressionService = new RegressionService();

    @FXML private TextArea resultsTextArea;
    @FXML private Label statusLabel;

    private ObservableList<DataPoint> dataPoints;

    public void setDataPoints(ObservableList<DataPoint> dataPoints) {
        this.dataPoints = dataPoints;
    }

    public void performRegressionAnalysis() {
        if (dataPoints == null || dataPoints.isEmpty()) {
            showError("Нет данных для анализа", "Пожалуйста, добавьте точки данных");
            return;
        }

        if (dataPoints.size() < 8 || dataPoints.size() > 12) {
            showError("Некорректное количество точек",
                    "Требуется от 8 до 12 точек. Сейчас: " + dataPoints.size());
            return;
        }

        try {
            RegressionResult result = regressionService.calculateAll(dataPoints);
            displayResults(result);
            updateStatus("Анализ завершен успешно");
        } catch (Exception ex) {
            showError("Ошибка вычислений", ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void displayResults(RegressionResult result) {
        StringBuilder sb = new StringBuilder();

        sb.append("РЕЗУЛЬТАТЫ АППРОКСИМАЦИИ\n");
        sb.append("========================================\n\n");

        sb.append("Наилучшая аппроксимация: ")
                .append(result.getBestFunction().getName())
                .append("\n\n");

        sb.append("КОЭФФИЦИЕНТЫ ФУНКЦИЙ:\n");
        for (FunctionApproximation fa : result.getAllFunctions()) {
            sb.append(String.format("%-15s: ", fa.getName()))
                    .append(fa.getCoefficientsAsString())
                    .append("\n");
        }
        sb.append("\n");

        sb.append("СТАТИСТИЧЕСКИЕ ПОКАЗАТЕЛИ:\n");
        sb.append("Функция            СКО (σ)     R²\n");
        sb.append("---------------------------------\n");
        for (FunctionApproximation fa : result.getAllFunctions()) {
            sb.append(String.format("%-15s %9.6f   %9.6f\n",
                    fa.getName(), fa.getStandardDeviation(), fa.getRSquared()));
        }
        sb.append("\n");

        sb.append("Коэффициент корреляции Пирсона (линейная): ")
                .append(String.format("%.6f\n\n", result.getPearsonCorrelation()));

        double rSquared = result.getBestFunction().getRSquared();
        sb.append("ИНТЕРПРЕТАЦИЯ R²: ");
        if (rSquared >= 0.95) {
            sb.append("Отличное соответствие (R² ≥ 0.95)");
        } else if (rSquared >= 0.85) {
            sb.append("Хорошее соответствие (0.85 ≤ R² < 0.95)");
        } else if (rSquared >= 0.7) {
            sb.append("Удовлетворительное соответствие (0.7 ≤ R² < 0.85)");
        } else {
            sb.append("Слабое соответствие (R² < 0.7)");
        }
        sb.append("\n\n");

        FunctionApproximation best = result.getBestFunction();
        sb.append("ДЕТАЛИ ДЛЯ ").append(best.getName()).append(":\n");
        sb.append("   X       Y       Ф(X)    Отклонение\n");
        sb.append("-------------------------------------\n");
        for (int i = 0; i < dataPoints.size(); i++) {
            DataPoint p = dataPoints.get(i);
            double actual = best.getCalculatedValues().get(i);
            double error = best.getErrors().get(i);
            sb.append(String.format("%7.4f  %7.4f  %7.4f  %7.4f\n",
                    p.getX(), p.getY(), actual, error));
        }

        resultsTextArea.setText(sb.toString());
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("Ошибка");
        alert.setHeaderText(title);
        alert.showAndWait();
        updateStatus("Ошибка: " + title);
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    public RegressionResult getLastResult() {
        return regressionService.getLastResult();
    }
}
