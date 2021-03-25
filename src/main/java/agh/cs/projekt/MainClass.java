package agh.cs.projekt;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainClass extends Application {

    public static void main(String[] args){
        System.out.println("Witam panstwa");
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("Cls "+getClass());
        System.out.println("Res "+getClass().getResource("/fxml/scene.fxml"));
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/scene.fxml"));

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setTitle("JavaFX and Gradle");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
