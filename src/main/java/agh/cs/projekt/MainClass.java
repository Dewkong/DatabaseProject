package agh.cs.projekt;

import agh.cs.projekt.models.ImageSource.*;
import agh.cs.projekt.models.*;
import agh.cs.projekt.ui.FXMLTourDetailsController;
import agh.cs.projekt.utils.ImageController;
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
        Tour t1 = null;
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();

            ImageSource testImg = new LocalImageSource("/test-img.jpg");
            session.save(testImg);
            t1 = new Tour("Wycieczka testowa", CountryEnum.POLAND, new Date(System.currentTimeMillis()), 10, 70.0f, "Lorem Ipsum dolor sit amet", testImg);
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

        //this must be done before a call to FXMLLoader.load
        //for now it's filled with example images
        ImageController.init(
                new HttpImageSource("https://www.elegantthemes.com/blog/wp-content/uploads/2020/08/000-http-error-codes.png"),
                new HttpImageSource("https://forum.bubble.io/uploads/default/original/3X/f/1/f1777bc40411988af0a87383e5f2fbde9c76ba9f.png")
        );

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/tour_details_scene.fxml"));
        Parent root = loader.load();
        FXMLTourDetailsController controller = loader.getController();
        controller.displayTour(t1);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setTitle("JavaFX and Gradle");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
