package agh.cs.projekt.ui;

import agh.cs.projekt.services.DatabaseHolder;
import agh.cs.projekt.services.UserHolder;
import agh.cs.projekt.models.ApplicationUser;
import agh.cs.projekt.models.RoleEnum;
import agh.cs.projekt.models.Tour;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.hibernate.Session;

import javax.persistence.Query;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class FXMLToursController implements Initializable {

    @FXML
    private Label userLabel;
    @FXML
    private Button logoutButton;
    @FXML
    private Label titleLabel;
    @FXML
    private GridPane gridMain;
    @FXML
    private Button addButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        UserHolder userHolder = UserHolder.getInstance();
        ApplicationUser user = userHolder.getUser();
        userLabel.setText("Zalogowano jako " + user.getLogin() + "#" + user.getCustomer().getId());
        titleLabel.setText("Wycieczki");
        titleLabel.setStyle("-fx-font-weight: bold");
        logoutButton.setText("Wyloguj");
        addButton.setText("Dodaj wycieczke");
        addButton.setVisible(user.getRole() == RoleEnum.ADMIN);

        DatabaseHolder databaseHolder = DatabaseHolder.getInstance();
        try (Session session = databaseHolder.getSession()) {
            session.beginTransaction();
            Query query_tours = session.createQuery("select T from Tour as T");
            @SuppressWarnings("unchecked") List<Tour> tours = query_tours.getResultList();
            session.getTransaction().commit();

            GridPane gridTours = new GridPane();
            gridTours.setAlignment(Pos.CENTER);
            gridTours.setStyle("-fx-border-insets: 5px");
            gridTours.setStyle("-fx-padding: 5px");
            gridTours.setStyle("-fx-background-insets: 5px");
            gridTours.setVgap(10);
            gridTours.setHgap(10);

            int i = 0;
            for (Tour tour : tours){
                GridPane gridTour = new GridPane();
                gridTour.setAlignment(Pos.CENTER);
                gridTour.setStyle("-fx-background-color: teal");
                gridTour.setPadding(new Insets(10, 10, 10, 10));

                Label labelName = new Label(tour.getName() + " : " + tour.getCountry());
                labelName.setAlignment(Pos.CENTER);
                labelName.setMinWidth(180);
                labelName.setMaxWidth(180);
                labelName.setMinHeight(40);
                Label labelDate = new Label("Data: " + tour.getTourDate().toString());
                labelDate.setAlignment(Pos.CENTER);
                labelDate.setMinWidth(180);
                labelDate.setMaxWidth(180);
                labelDate.setMinHeight(40);
                Label labelPrice = new Label("Cena: " + tour.getPrice());
                labelPrice.setAlignment(Pos.CENTER);
                labelPrice.setMinWidth(180);
                labelPrice.setMaxWidth(180);
                labelPrice.setMinHeight(40);
                Label labelPlaces = new Label();
                labelPlaces.setAlignment(Pos.CENTER);
                labelPlaces.setMinWidth(180);
                labelPlaces.setMaxWidth(180);
                labelPlaces.setMinHeight(40);
                try (Session session2 = databaseHolder.getSession()) {
                    session2.beginTransaction();
                    Query query_available = session2.createNativeQuery("select available_places(:queried_id) from dual").setParameter("queried_id", tour.getId());
                    @SuppressWarnings("unchecked") BigDecimal available = (BigDecimal) query_available.getSingleResult();
                    session2.getTransaction().commit();
                    labelPlaces.setText("Wolne miejsca: " + available + "/" + tour.getMaxPlaces());
                }

                Button detailsButton = new Button("Wiecej");
                detailsButton.setOnAction(e -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/tour_details_scene.fxml"));
                        Parent root = loader.load();
                        FXMLTourDetailsController controller = loader.getController();
                        controller.displayTour(tour);
                        logoutButton.getScene().setRoot(root);
                    } catch (IOException ioException) {
                        //TODO show some alert signaling the error ~W
                        new IOException("Error in FXML loader", ioException).printStackTrace();
                    }
                });

                HBox detailsBox = new HBox(detailsButton);
                detailsBox.setAlignment(Pos.CENTER);

                gridTour.add(labelName, 0, 0);
                gridTour.add(labelPrice, 0, 1);
                gridTour.add(labelDate, 0, 2);
                gridTour.add(labelPlaces, 0, 3);
                gridTour.add(detailsBox, 0, 4);
                gridTours.add(gridTour, i % 3, i / 3);
                i++;
            }
            gridMain.add(gridTours, 0, 1);
        }
    }

    public void logout(ActionEvent actionEvent) throws IOException {
        UserHolder userHolder = UserHolder.getInstance();
        userHolder.removeUser();
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login_scene.fxml"));
        logoutButton.getScene().setRoot(root);
    }

    public void addTour(ActionEvent actionEvent) throws IOException {
        System.out.println("TODO add tour");
    }
}
