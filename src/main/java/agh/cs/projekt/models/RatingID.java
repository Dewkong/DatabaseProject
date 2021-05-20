package agh.cs.projekt.models;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class RatingID implements Serializable {
    private Integer customerID;
    private Integer tourID;

    public RatingID() {
        //required by Hibernate
    }

    public Integer getCustomerID() {
        return customerID;
    }

    public void setCustomerID(Integer customerID) {
        this.customerID = customerID;
    }

    public Integer getTourID() {
        return tourID;
    }

    public void setTourID(Integer tourID) {
        this.tourID = tourID;
    }
}
