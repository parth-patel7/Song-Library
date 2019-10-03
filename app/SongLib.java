package app;

import java.io.IOException;
import controller.ViewController;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class SongLib extends Application{

	@Override
	public void start(Stage primaryStage) {

		System.out.println(ViewController.databaseAlreadyExists());

		// Try-Catch just to be safe.
		try {

			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/controller/AppView.fxml"));

			AnchorPane pane = loader.load();
			ViewController controller = loader.getController();

			Scene scene = new Scene(pane);
			primaryStage.setResizable(false);
			primaryStage.setScene(scene);
			primaryStage.show(); 



		} catch (IOException e) {
			e.printStackTrace();
		}

		/**
		 * After the user has closed the GUI the data base is being updated here 
		 */
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				try {
					ViewController.storeDataToFile();
					System.out.println("This closes amigo \n");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});



	}

	public static void main(String[] args) {
		launch(args);
	}



}
