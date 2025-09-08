package ru.ifmo.cs.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import ru.ifmo.cs.model.DataPoint;
import ru.ifmo.cs.model.functions.FunctionApproximation;

public class FileService {
    private static final FileChooser.ExtensionFilter TXT_FILTER = 
            new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt");
    private static final FileChooser.ExtensionFilter CSV_FILTER = 
            new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv");
    private static final FileChooser.ExtensionFilter ALL_FILTER = 
            new FileChooser.ExtensionFilter("All Files", "*.*");

    public File showOpenDialog(Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Открыть файл с данными");
        fileChooser.getExtensionFilters().addAll(TXT_FILTER, CSV_FILTER, ALL_FILTER);
        return fileChooser.showOpenDialog(owner);
    }

    public File showSaveDialog(Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить данные");
        fileChooser.getExtensionFilters().addAll(TXT_FILTER, CSV_FILTER, ALL_FILTER);
        return fileChooser.showSaveDialog(owner);
    }

    public File showSaveReportDialog(Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить отчет");
        fileChooser.getExtensionFilters().addAll(TXT_FILTER, ALL_FILTER);
        
        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        fileChooser.setInitialFileName("regression_report_" + timestamp + ".txt");
        
        return fileChooser.showSaveDialog(owner);
    }

    public List<DataPoint> loadPoints(File file) throws IOException {
        List<DataPoint> points = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("[,\\s]+");
                if (parts.length >= 2) {
                    try {
                        String xStr = parts[0].trim().replace(',', '.');
                        String yStr = parts[1].trim().replace(',', '.');
                        double x = Double.parseDouble(xStr);
                        double y = Double.parseDouble(yStr);
                        if (Double.isFinite(x) && Double.isFinite(y)) {
                            points.add(new DataPoint(x, y));
                        } else {
                            System.err.println("Невалидные значения (NaN/Inf) в строке: " + line);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Ошибка формата данных в строке: " + line);
                    }
                }
            }
        }
        return points;
    }

    public void savePoints(List<DataPoint> points, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (DataPoint point : points) {
                writer.printf("%.6f, %.6f%n", point.getX(), point.getY());
            }
        }
    }

    public void saveFullReport(List<DataPoint> points, RegressionResult result, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("Отчет по аппроксимации функций");
            writer.println("==============================");
            writer.println("Сгенерирован: " + new Date());
            writer.println("Количество точек: " + points.size());
            writer.println();
            
            writer.println("Исходные точки:");
            writer.println("X\tY");
            for (DataPoint p : points) {
                writer.printf("%.6f\t%.6f%n", p.getX(), p.getY());
            }
            writer.println();
            
            writer.println("Результаты аппроксимации:");
            writer.println("==============================");
            writer.println();
            
            writer.println("Наилучшая аппроксимация: " + result.getBestFunction().getName());
            writer.println();
            
            writer.println("Коэффициенты функций:");
            for (FunctionApproximation fa : result.getAllFunctions()) {
                writer.println(fa.getName() + ": " + fa.getCoefficientsAsString());
            }
            writer.println();
            
            writer.println("Среднеквадратичные отклонения и коэффициенты детерминации:");
            writer.println("Функция\tСКО\tR²");
            for (FunctionApproximation fa : result.getAllFunctions()) {
                writer.printf("%s\t%.6f\t%.6f%n", fa.getName(), fa.getStandardDeviation(), fa.getRSquared());
            }
            writer.println();
            
            writer.printf("Коэффициент корреляции Пирсона (линейная): %.6f%n", result.getPearsonCorrelation());
            writer.println();
            
            double rSquared = result.getBestFunction().getRSquared();
            writer.println("Интерпретация R²: ");
            if (rSquared >= 0.95) {
                writer.println("Отличное соответствие (R² ≥ 0.95)");
            } else if (rSquared >= 0.85) {
                writer.println("Хорошее соответствие (0.85 ≤ R² < 0.95)");
            } else if (rSquared >= 0.7) {
                writer.println("Удовлетворительное соответствие (0.7 ≤ R² < 0.85)");
            } else {
                writer.println("Слабое соответствие (R² < 0.7)");
            }
            writer.println();
            
            FunctionApproximation best = result.getBestFunction();
            writer.println("Детали для " + best.getName() + ":");
            writer.println("X\tY\tФ(X)\tОтклонение");
            for (int i = 0; i < points.size(); i++) {
                DataPoint p = points.get(i);
                double actual = best.getCalculatedValues().get(i);
                double error = best.getErrors().get(i);
                writer.printf("%.6f\t%.6f\t%.6f\t%.6f%n", p.getX(), p.getY(), actual, error);
            }
        }
    }
}