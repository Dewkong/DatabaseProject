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

    //returns the rating object for this reservation, or null if no reservation was made
    public Rating getRatingForReservation(){
        try (Session session = DatabaseHolder.getInstance().getSession()){
            Transaction transaction = session.beginTransaction();

            Rating rating = session.createQuery("from Rating where reservation = :reservation", Rating.class)
                    .setParameter("reservation", this)
                    .getSingleResult();
            transaction.commit();

            return rating;
        } catch (NoResultException e){
            //consume error
            return null;
        }
    }

    //adds a rating to a reservation, returns the new rating object, or null if value was 0 (rating was deleted)
    public Rating setRating(int value) throws Exception {
        Rating currentRating = this.getRatingForReservation();

        try(Session session = DatabaseHolder.getInstance().getSession()){
            Transaction transaction = session.beginTransaction();

            if (currentRating == null){
                //user hasn't rated yet
                if (value == 0){
                    //user doesn't want to add a rating
                    transaction.commit();
                    return null;
                } else {
                    //add new rating
                    Rating rating = new Rating(this, value);
                    session.save(rating);
                    transaction.commit();
                    return rating;
                }
            } else {
                //user wants to change the rating
                if (value == 0) {
                    //user wants to remove the rating
                    session.delete(currentRating);
                    transaction.commit();
                    return null;
                } else {
                    //user wants to alter the rating
                    currentRating.setRating(value);
                    session.update(currentRating);
                    transaction.commit();
                    return currentRating;
                }
            }
        }
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
