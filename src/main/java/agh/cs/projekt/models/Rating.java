package agh.cs.projekt.models;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Entity
public class Rating implements Serializable { //Hibernate requires implementation of Serializable to have a composite key here

    @EmbeddedId
    @ManyToOne
    @JoinColumn(nullable=false)
    private Customer customer;

    @EmbeddedId
    @ManyToOne
    @JoinColumn(nullable=false)
    private Tour tour;

    private int rating;

    public Rating() {
        //required by Hibernate
    }

    public Rating(Customer customer, Tour tour, int rating) {
        this.customer = customer;
        this.tour = tour;
        this.rating = rating;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Tour getTour() {
        return tour;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
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
                "customer=" + customer.getId() +
                ", tour=" + tour.getId() +
                ", rating=" + rating +
                '}';
    }
}
