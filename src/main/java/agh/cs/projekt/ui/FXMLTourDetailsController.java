package agh.cs.projekt.ui;

import agh.cs.projekt.models.Customer;
import agh.cs.projekt.models.Rating;
import agh.cs.projekt.models.Reservation;
import agh.cs.projekt.models.Tour;
import agh.cs.projekt.services.DatabaseHolder;
import agh.cs.projekt.services.NavigationService;
import agh.cs.projekt.services.UserHolder;
import agh.cs.projekt.utils.ImageController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Pair;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

public class FXMLTourDetailsController implements Initializable {

    @FXML
    public Label tour_name;
    @FXML
    public ImageView image_view;
    @FXML
    public Label tour_country;
    @FXML
    public Label tour_description;
    @FXML
    public Label tour_date;
    @FXML
    public Label tour_price;
    @FXML
    public Label tour_places;
    @FXML
    public Button return_button;
    @FXML
    public Label customer_loading;
    @FXML
    public Label customer_reservations;
    @FXML
    public Button button_make_reservation;
    @FXML
    public Button button_cancel_reservation;
    @FXML
    public HBox hbox_customer_reservations;
    @FXML
    public HBox hbox_reservation_controls;
    @FXML
    public Label tour_rating;
    @FXML
    public HBox hbox_rating_controls;
    @FXML
    public ChoiceBox<RatingEnum> customer_rating;

    //private caches
    private Tour tour = null;
    private Customer customer = null;
    private RatingEnum currentCustomerRating = RatingEnum.NONE;

    private boolean reservationsReady = false;
    private boolean ratingsReady = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        for(RatingEnum option : RatingEnum.values()) {
            customer_rating.getItems().add(option);
        }
    }

    public void displayTour(Tour tour){
        if (tour == null){
            System.err.println("Trying to display a null tour!");
            NavigationService.getInstance().goBack();
            return;
        }

        this.tour = tour;
        this.customer = UserHolder.getInstance().getUser().getCustomer();
        ImageController.loadFromSource(image_view, tour.getImage());
        tour_name.setText(tour.getName());
        tour_country.setText(tour.getCountry().toString());
        tour_description.setText(tour.getDescription());
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        tour_date.setText(sdf.format(tour.getTourDate()));
        tour_price.setText(String.format("%.2f", tour.getPrice()) + "z\u0142"); // \u0142 - unicode for ł

        updateReservationsUI();
        updateRatingsUI();

        customer_rating.setOnAction(this::changeRating);
    }

    //button callback
    public void goBack(ActionEvent actionEvent) {
        NavigationService.getInstance().goBack();
    }

    //button callback
    public void makeReservation(ActionEvent actionEvent) {
        DatabaseHolder.getInstance().dbCallWithAlert(
                "Dodawanie rezerwacji",
                "Proszę czekać, dokonujemy rezerwacji...",
                "Gotowe!",
                "Wystapił błąd. Rezerwacja nie została dokonana.",
                session -> {
                    Reservation reservation = customer.addReservation(tour);
                    if (reservation == null) throw new RuntimeException("Failed to add reservation");
                },
                () -> Platform.runLater(this::updateReservationsUI)
        );
    }

    //button callback
    public void cancelReservation(ActionEvent actionEvent) {
        DatabaseHolder.getInstance().dbCallWithAlert(
                "Anulowanie rezerwacji",
                "Proszę czekać,  anulujemy rezerwację...",
                "Gotowe!",
                "Wystapił błąd. Nie udało się anulować rezerwacji.",
                session -> {
                    Reservation reservation = customer.cancelLatestReservation(tour);
                    if (reservation == null) throw new RuntimeException("Failed to add reservation");
                },
                () -> Platform.runLater(this::updateReservationsUI)
        );
    }

    //called when rating is changed by the user
    public void changeRating(ActionEvent event){
        RatingEnum newValue = customer_rating.getValue();
        if (newValue != currentCustomerRating){
            DatabaseHolder.getInstance().dbCallWithAlert(
                    "Ocenianie wycieczki",
                    "Proszę czekać, zapisujemy ocenę...",
                    "Gotowe!",
                    "Wystapił błąd. Ocena nie została zapisana.",
                    session -> {
                        customer.rateTour(tour, newValue.toInt());
                    },
                    () -> Platform.runLater(this::updateRatingsUI)
            );
        }

        currentCustomerRating = newValue;
    }

    //fetches info about reservation/customer and updates the ui accordingly
    private void updateReservationsUI(){
        tour_places.setText("\u0141adowanie..."); // \u0141 - unicode for uppercase ł
        reservationsReady = false;
        updateLoadingUI();

        DatabaseHolder.getInstance().dbCallNonBlocking(
                session -> {
                    int availablePlaces = tour.getAvailablePlaces();
                    int reservations =  customer.getReservationsForTour(tour);
                    Platform.runLater(() -> {
                        //run on FX thread
                        reservationsReady = true;
                        setTourAvailablePlaces(availablePlaces);
                        setCustomerReservedPlaces(reservations);
                        updateLoadingUI();
                    });
                }
        );
    }

    //fetches info about ratings and updates the ui accordingly
    private void updateRatingsUI(){
        tour_rating.setText("\u0141adowanie..."); // \u0141 - unicode for uppercase ł
        ratingsReady = false;
        updateLoadingUI();

        DatabaseHolder.getInstance().dbCallNonBlocking(
                session -> {
                    Rating customerRating = customer.getRatingForTour(tour);
                    int customerRatingVal = customerRating == null ? 0 : customerRating.getRating();
                    currentCustomerRating = RatingEnum.values()[customerRatingVal];
                    Pair<Double, Long> pair = tour.getRating();
                    if (pair != null) {
                        double averageRating = pair.getKey();
                        long ratingsAmt = pair.getValue();
                        Platform.runLater(() -> {
                            //run on FX thread
                            ratingsReady = true;
                            setRatings(customerRatingVal, averageRating, ratingsAmt);
                            updateLoadingUI();
                        });
                    } else {
                        //will be caught immediately
                        throw new NullPointerException("Error when retrieving tour rating");
                    }
                },
                exception ->{
                    // on error
                    Platform.runLater(() -> {
                        ratingsReady = true;
                        setRatings(-1, -1, -1);
                        updateLoadingUI();
                    });
                }
        );

    }

    //hides/shows appropriate elements based on the state of the "xyzReady" flags
    private void updateLoadingUI(){
        if (reservationsReady && ratingsReady){
            //hide main loading label
            customer_loading.setVisible(false);
            customer_loading.setManaged(false);
        }
        else {
            //show main loading label
            customer_loading.setVisible(true);
            customer_loading.setManaged(true);
        }

        if (reservationsReady){
            hbox_reservation_controls.setVisible(true);
            hbox_reservation_controls.setManaged(true);
            hbox_customer_reservations.setVisible(true);
            hbox_customer_reservations.setManaged(true);
        } else {
            hbox_reservation_controls.setVisible(false);
            hbox_reservation_controls.setManaged(false);
            hbox_customer_reservations.setVisible(false);
            hbox_customer_reservations.setManaged(false);
        }

        if (ratingsReady){
            hbox_rating_controls.setVisible(true);
            hbox_rating_controls.setManaged(true);
        } else {
            hbox_rating_controls.setVisible(false);
            hbox_rating_controls.setManaged(false);
        }
    }

    //must be run on the FX Thread
    private void setTourAvailablePlaces(int availablePlaces){
        if (availablePlaces < 0){
            tour_places.setText("B\u0142\u0105d"); // \u0142 - unicode for ł \u0105 - unicode for ą
        } else if (availablePlaces == 0){
            tour_places.setText("Brak wolnych miejsc");
            button_make_reservation.setDisable(true);
        } else {
            tour_places.setText(availablePlaces + "/" + tour.getMaxPlaces());
            button_make_reservation.setDisable(false);
        }
    }

    //must be run on the FX Thread
    private void setCustomerReservedPlaces(int reservedPlaces){
        if (reservedPlaces < 0){
            customer_reservations.setText("B\u0142\u0105d"); // \u0142 - unicode for ł \u0105 - unicode for ą
            button_cancel_reservation.setDisable(true);
        } else if (reservedPlaces == 0){
            customer_reservations.setText(String.format("%d", reservedPlaces));
            button_cancel_reservation.setDisable(true);
        } else {
            customer_reservations.setText(String.format("%d", reservedPlaces));
            button_cancel_reservation.setDisable(false);
        }
    }

    //must be run on the FX Thread
    private void setRatings(int customerRating, double tourAverageRating, long tourRatingsAmt){
        if (tourAverageRating < 0 || tourRatingsAmt < 0){
            tour_rating.setText("B\u0142\u0105d"); // \u0142 - unicode for ł \u0105 - unicode for ą
        } else if (tourRatingsAmt == 0){
            tour_rating.setText("Brak ocen");
        } else {
            tour_rating.setText(String.format("%.2f", tourAverageRating) + " (ilo\u015B\u0107 ocen: " + tourRatingsAmt +")");
        }

        if (customerRating < 0){
            //error
            customer_rating.setDisable(true);
            customer_rating.setValue(null);
        } else {
            customer_rating.setValue(RatingEnum.values()[customerRating]);
        }
    }

    //must be run on the FX Thread
    private void showCustomerInfo(){
        customer_loading.setVisible(false);
        customer_loading.setManaged(false);
        hbox_customer_reservations.setVisible(true);
        hbox_reservation_controls.setVisible(true);
    }

    private enum RatingEnum {
        NONE,
        ONE,
        TWO,
        THREE,
        FOUR,
        FIVE;

        String[] names = {
                "Nie oceniono",
                "1 gwiazdka",
                "2 gwiazdki",
                "3 gwiazdki",
                "4 gwiazdki",
                "5 gwiazdek",
        };

        int toInt() { return ordinal(); }

        @Override
        public String toString() {
            return names[ordinal()];
        }
    }

}
