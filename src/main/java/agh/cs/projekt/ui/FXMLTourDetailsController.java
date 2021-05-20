package agh.cs.projekt.ui;

import agh.cs.projekt.UserHolder;
import agh.cs.projekt.models.Customer;
import agh.cs.projekt.models.Rating;
import agh.cs.projekt.models.Reservation;
import agh.cs.projekt.models.Tour;
import agh.cs.projekt.utils.ImageController;
import agh.cs.projekt.utils.PersistentAlert;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Pair;

import java.io.IOException;
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
            //TODO revert to previous screen ~W
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
    public void goBack(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/tours_screen.fxml"));
        return_button.getScene().setRoot(root);
    }

    //button callback
    public void makeReservation(ActionEvent actionEvent) {
        PersistentAlert alert = new PersistentAlert(
                AlertType.INFORMATION,
                "Dodawanie rezerwacji",
                "Prosz\u0119 czeka\u0107, dokonujemy rezerwacji...");
        alert.show();

        new Thread(() -> {
            try {
                Reservation reservation =  customer.addReservation(tour);
                if (reservation != null){
                    //reservation successful, refresh the ui with most recent data (in case there were changes)
                    Platform.runLater(() -> {
                        //run on FX Thread
                        alert.setHeaderText("Gotowe!");
                    });
                } else {
                    //there is some discrepancy in the displayed data - refresh the ui
                    Platform.runLater(() -> {
                        //run on FX Thread
                        alert.setHeaderText("Wystapi\u0142 b\u0142\u0105d. Rezerwacja nie zosta\u0142a dokonana.");
                    });
                }
            } catch (Exception e) {
                System.err.println("Error while making reservation");
                e.printStackTrace();
                Platform.runLater(() -> {
                    //run on FX Thread
                    alert.setHeaderText("Wystapi\u0142 b\u0142\u0105d. Rezerwacja nie została dokonana.");
                });
            } finally {
                //enable closing of the alert dialog
                Platform.runLater(() -> {
                    //run on FX thread
                    updateReservationsUI();
                    alert.enableClose();
                });
            }
        }).start();
    }

    //button callback
    public void cancelReservation(ActionEvent actionEvent) {
        PersistentAlert alert = new PersistentAlert(
                AlertType.INFORMATION,
                "Anulowanie rezerwacji",
                "Prosz\u0119 czeka\u0107, anulujemy rezerwacj\u0119...");
        alert.show();

        new Thread(() -> {
            try {
                Reservation reservation = customer.cancelLatestReservation(tour);
                if (reservation != null){
                    //reservation successful, refresh the ui with most recent data (in case there were changes)
                    Platform.runLater(() -> {
                        //run on FX Thread
                        alert.setHeaderText("Gotowe!");
                    });
                } else {
                    //there is some discrepancy in the displayed data - refresh the ui
                    Platform.runLater(() -> {
                        //run on FX Thread
                        alert.setHeaderText("Wystapi\u0142 b\u0142\u0105d. Nie uda\u0142o si\u0119 anulowa\u0107 rezerwacji.");
                    });
                }
            } catch (Exception e) {
                System.err.println("Error while canceling reservation");
                e.printStackTrace();
                Platform.runLater(() -> {
                    //run on FX Thread
                    alert.setHeaderText("Wystapi\u0142 b\u0142\u0105d. Nie uda\u0142o si\u0119 anulowa\u0107 rezerwacji.");
                });
            } finally {
                //enable closing of the alert dialog
                Platform.runLater(() -> {
                    //run on FX thread
                    updateReservationsUI();
                    alert.enableClose();
                });
            }
        }).start();
    }

    //called when rating is changed by the user
    public void changeRating(ActionEvent event){
        RatingEnum newValue = customer_rating.getValue();
        if (newValue != currentCustomerRating){
            //rating was changed
            PersistentAlert alert = new PersistentAlert(
                    AlertType.INFORMATION,
                    "Ocenianie wycieczki",
                    "Prosz\u0119 czeka\u0107, zapisujemy ocen\u0119...");
            alert.show();

            new Thread(() -> {
                try {
                    Rating rating = customer.rateTour(tour, newValue.toInt());
                    //rating modification successful, refresh the ui with most recent data
                    Platform.runLater(() -> {
                        //run on FX Thread
                        alert.setHeaderText("Gotowe!");
                    });
                } catch (Exception e) {
                    System.err.println("Error while changing rating");
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        //run on FX Thread
                        alert.setHeaderText("Wystapi\u0142 b\u0142\u0105d. Ocena nie została zapisana.");
                    });
                } finally {
                    //enable closing of the alert dialog
                    Platform.runLater(() -> {
                        //run on FX thread
                        updateRatingsUI();
                        alert.enableClose();
                    });
                }
            }).start();
        }

        currentCustomerRating = newValue;
    }

    //fetches info about reservation/customer and updates the ui accordingly
    private void updateReservationsUI(){
        tour_places.setText("\u0141adowanie..."); // \u0141 - unicode for uppercase ł
        reservationsReady = false;
        updateLoadingUI();

        new Thread(() -> {
            //fetch available/reserved places
            int availablePlaces = tour.getAvailablePlaces();
            int reservations =  customer.getReservationsForTour(tour);
            Platform.runLater(() -> {
                //run on FX thread
                reservationsReady = true;
                setTourAvailablePlaces(availablePlaces);
                setCustomerReservedPlaces(reservations);
                updateLoadingUI();
            });
        }).start();
    }

    //fetches info about ratings and updates the ui accordingly
    private void updateRatingsUI(){
        tour_rating.setText("\u0141adowanie..."); // \u0141 - unicode for uppercase ł
        ratingsReady = false;
        updateLoadingUI();

        new Thread(() -> {
            try {
                //fetch info about the tour's average rating and the customer's rating of this tour

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
            } catch (Exception e){
                //error
                System.err.println("Error when fetching ratings");
                e.printStackTrace();

                Platform.runLater(() -> {
                    ratingsReady = true;
                    setRatings(-1, -1, -1);
                    updateLoadingUI();
                });
            }
        }).start();
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
