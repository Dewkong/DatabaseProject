package agh.cs.projekt;

import agh.cs.projekt.models.CountryEnum;
import agh.cs.projekt.models.ImageSource.HttpImageSource;
import agh.cs.projekt.models.Tour;
import agh.cs.projekt.services.DatabaseHolder;
import agh.cs.projekt.services.NavigationService;
import agh.cs.projekt.services.UserHolder;
import agh.cs.projekt.utils.ImageController;
import javafx.application.Application;
import javafx.stage.Stage;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.sql.Date;

public class MainClass extends Application {

    public static void main(String[] args){
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        //establishing database connection for the first time
        //which means initializing DatabaseHolder
        DatabaseHolder databaseHolder = DatabaseHolder.getInstance();
        //initializing UserHolder
        UserHolder userHolder = UserHolder.getInstance();

        //initialising image processor
        //this must be done before a call to FXMLLoader.load
        //for now it's filled with example images
        ImageController.init(
                new HttpImageSource("https://www.elegantthemes.com/blog/wp-content/uploads/2020/08/000-http-error-codes.png"),
                new HttpImageSource("https://forum.bubble.io/uploads/default/original/3X/f/1/f1777bc40411988af0a87383e5f2fbde9c76ba9f.png")
        );

        //
        //javafx window initialisation:
        //

        NavigationService.getInstance().setInitialScene(primaryStage, "login_scene.fxml");
        primaryStage.setTitle("Biuro podr\u00F3\u017Cy");
        primaryStage.show();
    }
}
