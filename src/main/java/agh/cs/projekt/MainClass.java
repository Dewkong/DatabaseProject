package agh.cs.projekt;

import agh.cs.projekt.models.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.sql.Date;
import java.util.Arrays;
import java.util.List;

public class MainClass extends Application {

    public static void main(String[] args){
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        //
        //database initialisation + sample query:
        //

        //loads DB config from hibernate.cfg.xml
        Configuration config = new Configuration();
        config.configure();

        //getting an instance of the current DB session
        SessionFactory sessionFactory = config.buildSessionFactory();

        //saving some data
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();

            Tour t1 = new Tour("Wycieczka testowa", CountryEnum.POLAND, new Date(System.currentTimeMillis()), 10, 70.0f, "Lorem Ipsum dolor sit amet");
            session.save(t1);
            Customer c1 = new Customer("Jan", "Kowalski", "123456789", "test@example.com");
            session.save(c1);
            Rating r1 = new Rating(c1, t1, 5);
            session.save(r1);
            Reservation re1 = new Reservation(c1, t1, new Date(System.currentTimeMillis()), false);
            session.save(re1);
            Payment p1 = new Payment(c1, re1, new Date(System.currentTimeMillis()), 55.0f);
            session.save(p1);

            session.getTransaction().commit();
        } catch (PersistenceException e) {
            System.err.println("Hibernate encountered an error in transaction:");
            e.printStackTrace();
        }

        //example query
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();

            Query q1 = session.createQuery("from Rating");
            @SuppressWarnings("unchecked") List<Rating> resultList = q1.getResultList(); //suppress warning jest konieczny bo Hibernate nie zna typu zwrotu z Query w czasie kompilacji
            System.out.println("Zawartość tabeli 'Rating': " + Arrays.toString(resultList.toArray()));

            session.getTransaction().commit();
        }


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
