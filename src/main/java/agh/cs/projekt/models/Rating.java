package agh.cs.projekt.models;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.io.Serializable;

@Entity
public class Rating implements Serializable {

    @Id
    @OneToOne()
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Reservation reservation;

    private int rating;

    public Rating() {
        //required by Hibernate
    }

    public Rating(Reservation reservation, int rating) {
        this.reservation = reservation;
        this.rating = rating;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "Rating{" +
                "reservation=" + reservation +
                ", rating=" + rating +
                '}';
    }
}
