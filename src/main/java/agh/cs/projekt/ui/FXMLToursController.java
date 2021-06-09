package agh.cs.projekt.ui;

import agh.cs.projekt.models.*;
import agh.cs.projekt.services.DatabaseHolder;
import agh.cs.projekt.services.NavigationService;
import agh.cs.projekt.services.UserHolder;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;

import javax.persistence.Query;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private Button resetButton;
    @FXML
    private Button filterButton;
    @FXML
    private ChoiceBox<CountryEnum> countryBox;
    @FXML
    private TextField minPriceField;
    @FXML
    private TextField maxPriceField;
    @FXML
    private TextField availablePlacesField;

    //cache
    private List<Tour> allTours = null;
    private List<Tour> tours = new ArrayList<>();
    private GridPane gridTours;

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
        resetButton.setText("Reset");
        filterButton.setText("Filtruj");
        countryBox.getItems().add(null);
        minPriceField.setTextFormatter(new TextFormatter<>(new FloatStringConverter()));
        maxPriceField.setTextFormatter(new TextFormatter<>(new FloatStringConverter()));
        availablePlacesField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));

        gridTours = new GridPane();
        gridTours.setAlignment(Pos.TOP_CENTER);
        gridTours.setStyle("-fx-border-insets: 5px");
        gridTours.setStyle("-fx-padding: 5px");
        gridTours.setStyle("-fx-background-insets: 5px");
        gridTours.setVgap(10);
        gridTours.setHgap(10);
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.TOP_CENTER);

        ScrollPane scrollTours = new ScrollPane();
        scrollTours.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollTours.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollTours.setVmax(440);
        scrollTours.setMinWidth(640);
        scrollTours.setMaxWidth(640);
        scrollTours.setStyle("-fx-padding: 0px"); //fixes a bug in JavaFX that causes text to be blurred inside of ScrollPane
        scrollTours.setContent(gridTours);

        vbox.getChildren().add(scrollTours);
        gridMain.add(vbox, 0, 2);

        DatabaseHolder.getInstance().dbCallNonBlocking(
                session -> {
                    session.beginTransaction();
                    Query query_tours = session.createQuery("select T from Tour as T");
                    @SuppressWarnings("unchecked") List<Tour> result = query_tours.getResultList();
                    allTours = result;
                    tours.addAll(allTours);
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
            gridTour.setStyle("-fx-background-color: #4477FF");
            gridTour.setPadding(new Insets(10, 10, 10, 10));

            Label labelName = new Label(tour.getName() + " : " + tour.getCountry());
            labelName.setAlignment(Pos.CENTER);
            labelName.setMinWidth(180);
            labelName.setMaxWidth(180);
            labelName.setMinHeight(40);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            Label labelDate = new Label("Data: " + sdf.format(tour.getTourDate()));
            labelDate.setAlignment(Pos.CENTER);
            labelDate.setMinWidth(180);
            labelDate.setMaxWidth(180);
            labelDate.setMinHeight(40);
            Label labelPrice = new Label("Cena: " + String.format("%.2f", tour.getPrice()) + "z\u0142");
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

    public void loadTours(){
        loadToursUI();
        tours.clear();
        tours.addAll(allTours);
    }

    public void refreshToursUI(){
        gridTours.getChildren().removeAll(gridTours.getChildren());
        Platform.runLater(this::loadTours);
    }

    public void logout(ActionEvent actionEvent) {
        UserHolder userHolder = UserHolder.getInstance();
        userHolder.removeUser();

        NavigationService.getInstance().flushHistory();
        NavigationService.getInstance().setScene("login_scene.fxml");
    }

    public void resetFilters(ActionEvent actionEvent){
        countryBox.getSelectionModel().clearSelection();
        minPriceField.setText("");
        maxPriceField.setText("");
        availablePlacesField.setText("");
        refreshToursUI();
    }

    public void applyFilters(ActionEvent actionEvent){
        filterCountries();
        filterPrices();
        filterPlaces();
        refreshToursUI();
    }

    public void filterCountries(){
        if (countryBox.getValue() != null){
            tours.removeIf(tour -> tour.getCountry() != countryBox.getValue());
        }
    }

    public void filterPrices(){
        if (!minPriceField.getText().equals("")){
            tours.removeIf(tour -> tour.getPrice() < Float.parseFloat(minPriceField.getText()));
        }
        if (!maxPriceField.getText().equals("")){
            tours.removeIf(tour -> tour.getPrice() > Float.parseFloat(maxPriceField.getText()));
        }
    }

    public void filterPlaces(){
        if (!availablePlacesField.getText().equals("")){
            tours.removeIf(tour -> tour.getAvailablePlaces() < Float.parseFloat(availablePlacesField.getText()));
        }
    }

    public void addTour(ActionEvent actionEvent) throws IOException {
        System.out.println("TODO add tour");
    }
}
