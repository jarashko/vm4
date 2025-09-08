package ru.ifmo.cs.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;
import ru.ifmo.cs.model.DataPoint;
import ru.ifmo.cs.model.functions.FunctionApproximation;
import ru.ifmo.cs.service.FileService;
import ru.ifmo.cs.service.RegressionResult;
import ru.ifmo.cs.service.RegressionService;

public class MainController {
    @FXML
    private TableView<DataPoint> pointsTable;
    @FXML
    private TableColumn<DataPoint, Double> xColumn;
    @FXML
    private TableColumn<DataPoint, Double> yColumn;

    @FXML
    private TextField xInput;
    @FXML
    private TextField yInput;
    @FXML
    private Button addUpdateButton;

    @FXML
    private LineChart<Number, Number> chart;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    @FXML
    private TextArea resultsArea;
    @FXML
    private Label statusBar;

    private final ObservableList<DataPoint> dataPoints = FXCollections.observableArrayList();
    private RegressionController regressionController;

    private final FileService fileService = new FileService();
    private final RegressionService regressionService = new RegressionService();
    private RegressionResult lastResult;
    
    private DataPoint editingPoint = null; // Точка, которую мы редактируем

    @FXML
    public void initialize() {
        xColumn.setCellValueFactory(new PropertyValueFactory<>("x"));
        yColumn.setCellValueFactory(new PropertyValueFactory<>("y"));
        
        // Настройка редактируемых ячеек
        xColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        yColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        
        // Обработчики для редактирования
        xColumn.setOnEditCommit(event -> {
            DataPoint point = event.getRowValue();
            point.setX(event.getNewValue());
            updateStatus("Точка обновлена: (" + point.getX() + ", " + point.getY() + ")");
        });
        
        yColumn.setOnEditCommit(event -> {
            DataPoint point = event.getRowValue();
            point.setY(event.getNewValue());
            updateStatus("Точка обновлена: (" + point.getX() + ", " + point.getY() + ")");
        });
        
        pointsTable.setItems(dataPoints);

        chart.setAnimated(false);
        chart.setCreateSymbols(false);
        chart.setLegendVisible(true);

        clearAll();
    }

    @FXML
    private void handleAddPoint() {
        try {
            double x = parseDouble(xInput.getText(), "X");
            double y = parseDouble(yInput.getText(), "Y");

            if (editingPoint != null) {
                // Режим редактирования - обновляем существующую точку
                editingPoint.setX(x);
                editingPoint.setY(y);
                editingPoint = null;
                addUpdateButton.setText("Добавить");
                clearInputs();
                updateStatus("Точка обновлена: (" + x + ", " + y + ")");
            } else {
                // Режим добавления - добавляем новую точку
                dataPoints.add(new DataPoint(x, y));
                clearInputs();
                updateStatus("Точка добавлена: (" + x + ", " + y + ")");
            }
        } catch (NumberFormatException e) {
            updateStatus("Ошибка: " + e.getMessage());
        }
    }

    @FXML
    private void handleRemovePoint() {
        ObservableList<DataPoint> selectedPoints = pointsTable.getSelectionModel().getSelectedItems();

        if (selectedPoints.isEmpty()) {
            updateStatus("Ошибка: Не выбраны точки для удаления");
            return;
        }

        dataPoints.removeAll(selectedPoints);
        updateStatus("Удалено точек: " + selectedPoints.size());
    }

    @FXML
    private void handleEditPoint() {
        DataPoint selectedPoint = pointsTable.getSelectionModel().getSelectedItem();

        if (selectedPoint == null) {
            updateStatus("Ошибка: Не выбрана точка для редактирования");
            return;
        }

        // Устанавливаем режим редактирования
        editingPoint = selectedPoint;
        addUpdateButton.setText("Обновить");
        
        // Заполняем поля ввода текущими значениями выбранной точки
        xInput.setText(String.valueOf(selectedPoint.getX()));
        yInput.setText(String.valueOf(selectedPoint.getY()));
        
        updateStatus("Режим редактирования. Измените значения и нажмите 'Обновить'");
    }

    @FXML
    private void handleCancelEdit() {
        editingPoint = null;
        clearInputs();
        addUpdateButton.setText("Добавить");
        updateStatus("Режим редактирования отменен");
    }

    @FXML
    private void handleLoadFile() {
        File file = fileService.showOpenDialog(pointsTable.getScene().getWindow());
        if (file == null)
            return;

        try {
            List<DataPoint> points = fileService.loadPoints(file);
            dataPoints.setAll(points);
            updateStatus("Загружено точек: " + points.size());
        } catch (Exception e) {
            updateStatus("Ошибка загрузки: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveFile() {
        if (dataPoints.isEmpty()) {
            updateStatus("Ошибка: Нет данных для сохранения");
            return;
        }

        File file = fileService.showSaveDialog(pointsTable.getScene().getWindow());
        if (file == null)
            return;

        try {
            fileService.savePoints(dataPoints, file);
            updateStatus("Данные сохранены в: " + file.getName());
        } catch (Exception e) {
            updateStatus("Ошибка сохранения: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveReport() {
        if (lastResult == null) {
            updateStatus("Ошибка: Сначала выполните расчет");
            return;
        }
        
        File file = fileService.showSaveReportDialog(pointsTable.getScene().getWindow());
        if (file == null) return;

        try {
            fileService.saveFullReport(dataPoints, lastResult, file);
            updateStatus("Отчет сохранен в: " + file.getName());
        } catch (IOException e) {
            updateStatus("Ошибка сохранения отчета: " + e.getMessage());
        }
    }

    @FXML
    private void handleCalculate() {
        if (dataPoints.size() < 8) {
            updateStatus("Ошибка: Минимум 8 точек требуется");
            return;
        }

        if (dataPoints.size() > 12) {
            updateStatus("Ошибка: Максимум 12 точек разрешено");
            return;
        }

        try {
            RegressionResult result = regressionService.calculateAll(dataPoints);

            lastResult = result;

            displayResults(result);

            plotGraphs(result);

            updateStatus("Расчеты завершены успешно");
        } catch (Exception e) {
            updateStatus("Ошибка расчета: " + e.getMessage());
            e.printStackTrace();
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

        resultsArea.setText(sb.toString());
    }

    private void plotGraphs(RegressionResult result) {
        chart.getData().clear();

        double dataMinX, dataMaxX;
        if (dataPoints.isEmpty()) {
            dataMinX = 0.0;
            dataMaxX = 1.0;
        } else {
            dataMinX = dataPoints.stream().mapToDouble(DataPoint::getX).min().getAsDouble();
            dataMaxX = dataPoints.stream().mapToDouble(DataPoint::getX).max().getAsDouble();
        }

        if (dataPoints.size() <= 1 || Math.abs(dataMaxX - dataMinX) < 1e-9) {
            double centerX = dataPoints.isEmpty() ? 0.5 : dataPoints.get(0).getX();
            dataMinX = centerX - 0.5;
            dataMaxX = centerX + 0.5;
        }


        for (FunctionApproximation fa : result.getAllFunctions()) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(fa.getName());

            double currentPlotMinX = dataMinX;
            double currentPlotMaxX = dataMaxX;
            double plotRange = currentPlotMaxX - currentPlotMinX;

            double extendedMinX = currentPlotMinX - plotRange * 0.2;
            double extendedMaxX = currentPlotMaxX + plotRange * 0.2;

            if (Math.abs(extendedMaxX - extendedMinX) < 1e-9) {
                extendedMinX = currentPlotMinX - 0.5;
                extendedMaxX = currentPlotMinX + 0.5;
            }

            boolean isLogarithmic = fa.getName() != null && fa.getName().toLowerCase().contains("логарифм");

            if (isLogarithmic) {
                if (extendedMinX <= 1e-9) {
                    extendedMinX = 1e-9;
                }
                if (extendedMaxX <= extendedMinX) {
                    extendedMaxX = extendedMinX + 1.0;
                }
            }

            int numberOfPlotPoints = 200;
            for (int i = 0; i <= numberOfPlotPoints; i++) {
                double x = extendedMinX + i * (extendedMaxX - extendedMinX) / numberOfPlotPoints;

                if (isLogarithmic && x <= 0) {
                    continue;
                }

                double y = fa.calculate(x);

                if (!Double.isNaN(y) && Double.isFinite(y)) {
                    series.getData().add(new XYChart.Data<>(x, y));
                }
            }

            if (!series.getData().isEmpty()) {
                chart.getData().add(series);
            }
        }

        xAxis.setAutoRanging(true);
        yAxis.setAutoRanging(true);
    }

    private void clearInputs() {
        xInput.clear();
        yInput.clear();
    }

    private void clearAll() {
        dataPoints.clear();
        chart.getData().clear();
        resultsArea.clear();
        statusBar.setText("Готово");
    }

    private double parseDouble(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new NumberFormatException("Поле '" + fieldName + "' пустое");
        }
        try {
            String normalizedValue = value.trim().replace(',', '.');
            return Double.parseDouble(normalizedValue);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Неверный формат числа в поле '" + fieldName + "'");
        }
    }

    private void updateStatus(String message) {
        statusBar.setText(message);
    }

    public void handleAddPointFromInput(ActionEvent actionEvent) {

    }
}
