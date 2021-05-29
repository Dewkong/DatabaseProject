package agh.cs.projekt.models;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class ReservationID implements Serializable {
    private Integer customerID;
    private Integer tourID;

    public ReservationID() {
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
