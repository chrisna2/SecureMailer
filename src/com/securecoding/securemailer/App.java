package com.securecoding.securemailer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App extends Application {
	public static Logger logger = Logger.getLogger(App.class.getName());
    public static Stage stage;
    @Override
    public void start(Stage stage) {    
        try {
        	Parent root = FXMLLoader.load(getClass().getResource("/resources/fxml/Main.fxml"));
            stage.setScene(new Scene(root));
        } catch(IOException ioe) {
        	
        	//패치방안 1 - 주석처리
        	//ioe.printStackTrace();
        	
        	//패치방안 2 - Logging
        	logger.log(Level.INFO, ioe.getMessage(), ioe);
        }

        this.stage = stage;
        stage.setTitle("Secure Mailer");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
