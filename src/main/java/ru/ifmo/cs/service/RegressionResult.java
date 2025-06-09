package ru.ifmo.cs.service;

import ru.ifmo.cs.model.functions.FunctionApproximation;

import java.util.List;

public class RegressionResult {
    private List<FunctionApproximation> allFunctions;
    private FunctionApproximation bestFunction;
    private double pearsonCorrelation;

    public List<FunctionApproximation> getAllFunctions() {
        return allFunctions;
    }

    public void setAllFunctions(List<FunctionApproximation> allFunctions) {
        this.allFunctions = allFunctions;
    }

    public FunctionApproximation getBestFunction() {
        return bestFunction;
    }

    public void setBestFunction(FunctionApproximation bestFunction) {
        this.bestFunction = bestFunction;
    }

    public double getPearsonCorrelation() {
        return pearsonCorrelation;
    }

    public void setPearsonCorrelation(double pearsonCorrelation) {
        this.pearsonCorrelation = pearsonCorrelation;
    }
}
