package com.protolinux;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public Stage window;
    public MainWindow scene;
    public int WIDTH  = 360;
    public int HEIGHT = WIDTH / 12 * 12;

    public static void main(String[] args) {
	    launch(args);
    }

    @Override
    public void start(Stage _window) {
        window = _window;
        scene = new MainWindow();

        window.setTitle("UDP Jaynicorn (mini)");
        window.setScene(new Scene(scene, WIDTH, HEIGHT));
        window.show();
    }
}
