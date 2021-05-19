package agh.cs.projekt.ui;

import agh.cs.projekt.models.Tour;
import agh.cs.projekt.utils.ImageController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class FXMLTourDetailsController implements Initializable {

    @FXML
    public Label tour_name;
    @FXML
    public ImageView image_view;
    @FXML
    public Label tour_country;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //TODO

    }

    public void displayTour(Tour tour){

        ImageController.loadFromSource(image_view, tour.getImage());
        tour_name.setText(tour.getName());
        tour_country.setText(tour.getCountry().toString());

    }

}
