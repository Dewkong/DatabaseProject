package agh.cs.projekt.models;

import agh.cs.projekt.services.DatabaseHolder;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.*;

@Entity
public class Reservation {

    @EmbeddedId
    private ReservationID reservationID;

    @MapsId("customerID")
    @ManyToOne(optional = false)
    @JoinColumns(value = {
            @JoinColumn(name = "customerID", referencedColumnName = "id") })
    private Customer customer;

    @MapsId("tourID")
    @ManyToOne(optional = false)
    @JoinColumns(value = {
            @JoinColumn(name = "tourID", referencedColumnName = "id") })
    private Tour tour;

    private int reservedPlaces;

    public Reservation() {
        //required by Hibernate
        this.reservationID = new ReservationID();
    }

    public Reservation(Customer customer, Tour tour, int reservedPlaces) {
        this.customer = customer;
        this.tour = tour;
        this.reservedPlaces = reservedPlaces;
        this.reservationID = new ReservationID();
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

    public int getReservedPlaces() {
        return reservedPlaces;
    }

    public void setReservedPlaces(int reservedPlaces) {
        this.reservedPlaces = reservedPlaces;
    }

    // redundant, kept for compatibility
    public int getReservedAmount(){
        return getReservedPlaces();
    }

    public Reservation setPlacesAndPersist(int places){
        try (Session session = DatabaseHolder.getInstance().getSession()){
            Transaction transaction = session.beginTransaction();
            setReservedPlaces(places);
            if (places > 0) {
                session.save(this);
                transaction.commit();
                return this;
            } else if (places == 0){
                session.delete(this);
                transaction.commit();
                return null;
            } else {
                throw new IllegalArgumentException("Places can't be negative");
            }
        }
    }

    public Reservation changePlacesAndPersist(int placesDelta){
        return setPlacesAndPersist(this.getReservedPlaces() + placesDelta);
    }


    @Override
    public String toString() {
        return "Reservation{" +
                ", customer=" + customer +
                ", tour=" + tour +
                ", reservedPlaces=" + reservedPlaces +
                '}';
    }
}
