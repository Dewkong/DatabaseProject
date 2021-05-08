package agh.cs.projekt.models;

import javax.persistence.*;
import java.sql.Date;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne
    @JoinColumn(nullable=false)
    private Customer customer;


    @ManyToOne
    @JoinColumn(nullable=false)
    private Tour tour;

    @Column(nullable = false)
    private Date reservationDate;

    private boolean isCanceled;

    public Reservation() {
        //required by Hibernate
    }

    public Reservation(Customer customer, Tour tour, Date reservationDate, boolean isCanceled) {
        this.customer = customer;
        this.tour = tour;
        this.reservationDate = reservationDate;
        this.isCanceled = isCanceled;
    }

    public int getId() {
        return id;
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

    public Date getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(Date reservationDate) {
        this.reservationDate = reservationDate;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void setCanceled(boolean canceled) {
        isCanceled = canceled;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", customer=" + customer.getId() +
                ", tour=" + tour.getId() +
                ", reservationDate=" + reservationDate +
                ", isCanceled=" + isCanceled +
                '}';
    }

}
