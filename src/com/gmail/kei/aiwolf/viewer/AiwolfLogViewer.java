package com.gmail.kei.aiwolf.viewer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AiwolfLogViewer extends Application{
	private static final double WIDTH = 800.0;
	private static final double HEIGHT = 600.0;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("aiwolfviewer.fxml"));

		Scene scene = new Scene(root, WIDTH, HEIGHT);

		stage.setTitle("AI-Wolf Log Viewer ver.0.0.4");
		stage.setScene(scene);
		stage.show();
	}

}
