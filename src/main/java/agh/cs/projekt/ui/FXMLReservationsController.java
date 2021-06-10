package agh.cs.projekt.ui;

import agh.cs.projekt.models.ApplicationUser;
import agh.cs.projekt.models.Customer;
import agh.cs.projekt.models.Payment;
import agh.cs.projekt.models.Reservation;
import agh.cs.projekt.services.DatabaseHolder;
import agh.cs.projekt.services.NavigationService;
import agh.cs.projekt.services.UserHolder;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.hibernate.Session;

import javax.persistence.Query;
import java.net.URL;
import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class FXMLReservationsController implements Initializable {
    @FXML
    private Button returnButton;
    @FXML
    private Label titleLabel;
    @FXML
    private Button logoutButton;
    @FXML
    private Label userLabel;
    @FXML
    private ScrollPane reservationsScrollPane;
    @FXML
    private GridPane reservationsGrid;

    private Customer customer = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        returnButton.setText("Powrót");
        titleLabel.setText("Twoje rezerwacje");
        titleLabel.setFont(new Font(36));
        titleLabel.setStyle("-fx-font-weight: bold");
        logoutButton.setText("Wyloguj");
        ApplicationUser user = UserHolder.getInstance().getUser();
        userLabel.setText("Zalogowano jako " + user.getLogin() + "#" + user.getCustomer().getId());
        reservationsScrollPane.setStyle("-fx-padding: 0px");
        Platform.runLater(() -> titleLabel.getParent().requestFocus());
    }

    public void goBack(ActionEvent actionEvent) {
        NavigationService.getInstance().goBack();
    }

    public void logout(ActionEvent actionEvent) {
        UserHolder.getInstance().removeUser();

        NavigationService.getInstance().flushHistory();
        NavigationService.getInstance().setScene("login_scene.fxml");
    }

    public void loadReservations(Customer customer) {
        reservationsGrid.getChildren().clear();
        if (this.customer == null) this.customer = customer;
        Session session = DatabaseHolder.getInstance().getSession();
        session.beginTransaction();
        Query reservationsQuery = session.createQuery("select R from Reservation R where R.customer = :customer order by R.tour.tourDate").setParameter("customer", customer);
        @SuppressWarnings("unchecked") List<Reservation> customerReservations = reservationsQuery.getResultList();
        session.getTransaction().commit();

        reservationsGrid.setAlignment(Pos.CENTER);

        int rowIndex = 0;
        int reservationIndex = 0;
        for (Reservation reservation : customerReservations) {
            reservationsGrid.add(new Label(""), 0, rowIndex, 1, 2);

            Label tourName = new Label(reservation.getTour().getName());
            reservationsGrid.add(tourName, 1, rowIndex, 1, 1);

            Label tourDestination = new Label("Kraj: " + reservation.getTour().getCountry().toString());
            reservationsGrid.add(tourDestination, 1, rowIndex + 1, 1, 1);

            Label tourDate = new Label("Data wycieczki: " + reservation.getTour().getTourDate().toString());
            reservationsGrid.add(tourDate, 2, rowIndex, 1, 1);

            Label places = new Label("Ilość zarezerwowanych miejsc: " + reservation.getReservedPlaces());
            reservationsGrid.add(places, 2, rowIndex + 1, 1, 1);

            Label currentPayment = new Label("Zapłacono: " + reservation.getCurrentlyPaidAmount() + " / " + reservation.getTotalPrice() + "zł");
            reservationsGrid.add(currentPayment, 3, rowIndex, 1, 1);

            Label totalPrice = new Label("Pozostało do zapłaty: " + reservation.getRemainingPayment() + "zł");
            reservationsGrid.add(totalPrice, 3, rowIndex + 1, 1, 1);

            Button payment = new Button("Opłać rezerwację");
            payment.setOnAction(e -> makePayment(reservation));
            payment.setDisable(reservation.getRemainingPayment() <= 0);
            reservationsGrid.add(payment, 4, rowIndex, 1, 2);

            reservationIndex += 1;
            if (reservationIndex < customerReservations.size()) {
                Line line = new Line(0, 0, 595, 0);
                line.setStroke(Color.LIGHTGRAY);
                reservationsGrid.add(line, 1,rowIndex + 2, 5, 1);
                rowIndex += 1;
            }
            rowIndex += 2;
        }
    }

    public void makePayment(Reservation reservation) {
        float remainingPayment = reservation.getRemainingPayment();

        TextInputDialog dialog = new TextInputDialog(Float.toString(reservation.getRemainingPayment()));
        dialog.getDialogPane().setMinWidth(300);
        dialog.setTitle("Dokonywanie płatności");
        dialog.setHeaderText("Podaj kwotę wpłaty");
        dialog.setContentText("");

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        TextField inputField = dialog.getEditor();
        BooleanBinding isInvalid = Bindings.createBooleanBinding(
                () -> !inputField.getText().matches("^[\\d]+(\\.[\\d]{0,2}){0,1}$")
                        || Float.parseFloat(inputField.getText()) <= 0
                        || Float.parseFloat(inputField.getText()) > remainingPayment,
                inputField.textProperty());
        okButton.disableProperty().bind(isInvalid);

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            Payment payment = new Payment(reservation, new Date(new java.util.Date().getTime()), Float.parseFloat(result.get()));
            Session session = DatabaseHolder.getInstance().getSession();
            session.beginTransaction();
            session.save(payment);
            session.getTransaction().commit();
            System.out.println(payment);

            Alert popup = new Alert(Alert.AlertType.INFORMATION);
            popup.setTitle("Płatność");
            popup.setHeaderText("Prosimy o cierpliwość.");
            popup.setContentText("Trwa przetwarzanie płatności...");
            popup.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(e -> {
                popup.setHeaderText("Płatność przyjęta pomyślnie.");
                popup.setContentText("Płatność zaakceptowana.");
                popup.getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
                this.loadReservations(this.customer);
            });
            popup.show();
            delay.play();
        }
    }
}
