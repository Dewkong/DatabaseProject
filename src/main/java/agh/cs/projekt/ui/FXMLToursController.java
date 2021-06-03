package agh.cs.projekt.ui;

import agh.cs.projekt.models.*;
import agh.cs.projekt.services.DatabaseHolder;
import agh.cs.projekt.services.NavigationService;
import agh.cs.projekt.services.UserHolder;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.hibernate.Session;
import org.hibernate.Transaction;

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
    @FXML
    private Button testButton;
    @FXML
    private ChoiceBox<CountryEnum> countryBox;

    //cache
    List<Tour> tours = null;
    GridPane gridTours;

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
        testButton.setText("Test filtr");

        gridTours = new GridPane();
        gridTours.setAlignment(Pos.TOP_CENTER);
        gridTours.setStyle("-fx-border-insets: 5px");
        gridTours.setStyle("-fx-padding: 5px");
        gridTours.setStyle("-fx-background-insets: 5px");
        gridTours.setVgap(10);
        gridTours.setHgap(10);
        gridMain.add(gridTours, 0, 2);

        DatabaseHolder.getInstance().dbCallNonBlocking(
                session -> {
                    session.beginTransaction();
                    Query query_tours = session.createQuery("select T from Tour as T");
                    @SuppressWarnings("unchecked") List<Tour> result = query_tours.getResultList();
                    tours = result;
                    session.getTransaction().commit();
                },
                () -> Platform.runLater(this::loadToursUI)
        );
    }

    public void loadToursUI(){
        int i = 0;
        for (Tour tour : tours) {
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
            labelPlaces.setText("Wolne miejsca: " + tour.getAvailablePlaces() + "/" + tour.getMaxPlaces());

            Button detailsButton = new Button("Więcej");
            detailsButton.setOnAction(e -> {
                NavigationService.getInstance().setScene(
                        "tour_details_scene.fxml",
                        (FXMLTourDetailsController controller) -> {
                            controller.displayTour(tour);
                        });
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

            if (!countryBox.getItems().contains(tour.getCountry())){
                countryBox.getItems().add(tour.getCountry());
            }
        }
    }

    public void refreshToursUI(){
        gridTours.getChildren().removeAll(gridTours.getChildren());
        Platform.runLater(this::loadToursUI);
    }

    public void logout(ActionEvent actionEvent) {
        UserHolder userHolder = UserHolder.getInstance();
        userHolder.removeUser();

        NavigationService.getInstance().flushHistory();
        NavigationService.getInstance().setScene("login_scene.fxml");
    }

    public void testTour(ActionEvent actionEvent){
        tours.removeIf(tour -> tour.getCountry() == CountryEnum.CANADA);
        refreshToursUI();
    }

    public void filterCountries(){
        tours.removeIf(tour -> tour.getCountry() != countryBox.getValue());
        refreshToursUI();
    }

    public void countryChosen(ActionEvent actionEvent){
        System.out.println(countryBox.getValue());
        DatabaseHolder.getInstance().dbCallNonBlocking(
                session -> {
                    session.beginTransaction();
                    Query query_tours = session.createQuery("select T from Tour as T");
                    @SuppressWarnings("unchecked") List<Tour> result = query_tours.getResultList();
                    tours = result;
                    session.getTransaction().commit();
                },
                () -> Platform.runLater(this::filterCountries));
    }

    public void addTour(ActionEvent actionEvent) throws IOException {
        System.out.println("TODO add tour");
    }
}
