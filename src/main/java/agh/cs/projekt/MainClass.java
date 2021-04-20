package agh.cs.projekt;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.persistence.Query;

public class MainClass extends Application {

    public static void main(String[] args){
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("Cls "+getClass());
        System.out.println("Res "+getClass().getResource("/fxml/scene.fxml"));

        //
        //database initialisation + sample query:
        //

        //loads DB config from hibernate.cfg.xml
        Configuration config = new Configuration();
        config.configure();

        //getting an instance of the current DB session
        SessionFactory sessionFactory = config.buildSessionFactory();
        Session session = sessionFactory.getCurrentSession();

        session.beginTransaction();

        Query q = session.createSQLQuery("SELECT count(*) FROM TRIP");
        System.out.println("Ilość wycieczek w tabeli TRIP: " + q.getResultList().get(0));

        session.getTransaction().commit();
        session.close();


        //
        //javafx window initialisation:
        //

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/scene.fxml"));

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setTitle("JavaFX and Gradle");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
