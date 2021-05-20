package agh.cs.projekt.ui;

import agh.cs.projekt.models.Tour;
import agh.cs.projekt.utils.ImageController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

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

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    public void displayTour(Tour tour){

        ImageController.loadFromSource(image_view, tour.getImage());
        tour_name.setText(tour.getName());
        tour_country.setText(tour.getCountry().toString());
        tour_description.setText(tour.getDescription());
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        tour_date.setText(sdf.format(tour.getTourDate()));
        tour_price.setText(String.format("%.2f", tour.getPrice()) + "z\u0142"); // \u0142 - unicode for ł


    }

}
