package ru.ifmo.cs;

import javafx.application.Application;
import javafx.stage.Stage;
import ru.ifmo.cs.view.MainView;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainView mainView = new MainView();

        mainView.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}