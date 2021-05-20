package agh.cs.projekt.models;

import agh.cs.projekt.DatabaseHolder;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Embeddable
public class Customer implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String email;

    public Customer() {
        //required by Hibernate
    }

    public Customer(String name, String surname, String phoneNumber, String email) {
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    //returns the non-canceled reserved places on a tour, or a negative number on error
    public int getReservationsForTour(Tour tour){
        try(Session session = DatabaseHolder.getInstance().getSession()) {
            Transaction transaction = session.beginTransaction();

            Query<Reservation> query = session.createQuery("from Reservation where tour = :tour and customer = :customer", Reservation.class)
                    .setParameter("tour", tour)
                    .setParameter("customer", this);
            List<Reservation> reservations = query.getResultList();
            int reservedPlaces = 0;
            for (Reservation r : reservations){
                reservedPlaces += r.getReservedAmount();
            }

            transaction.commit();
            return reservedPlaces;
        } catch (Exception e){
            System.err.println("Error when fetching number of reservations");
            e.printStackTrace();
            return -1;
        }
    }

    //returns the newly created reservation if reservation was successfully added, or reservation couldn't be added
    public Reservation addReservation(Tour tour) throws Exception {
        try(Session session = DatabaseHolder.getInstance().getSession()){
            Transaction transaction = session.beginTransaction();
            Reservation reservation = new Reservation(this, tour);

            session.save(reservation);

            Query<Reservation> query = session.createQuery("from Reservation where tour = :tour", Reservation.class).setParameter("tour", tour);
            int presentReservations = 0;
            for (Reservation r : query.getResultList()){
                presentReservations += r.getReservedAmount();
            }

            if (presentReservations > tour.getMaxPlaces()){
                //too many reservations, rollback the reservation
                transaction.rollback();
                return null;
            } else {
                //reservation is legal, keep it
                transaction.commit();
                return reservation;
            }
        } catch (Exception e){
            throw new Exception("Error while making a new reservation", e);
        }
    }

    //returns the canceled reservation
    public Reservation cancelLatestReservation(Tour tour) throws Exception {
        try(Session session = DatabaseHolder.getInstance().getSession()){
            Transaction transaction = session.beginTransaction();

            Query<Reservation> query = session.createQuery("from Reservation where tour = :tour and customer = :customer order by reservationDate desc", Reservation.class)
                    .setParameter("tour", tour)
                    .setParameter("customer", this);
            List<Reservation> reservations = query.getResultList();
            if (reservations.size() == 0){
                transaction.rollback();
                throw new RuntimeException("No reservations to cancel");
            } else {
                Reservation latest = reservations.get(0);
                latest.setCanceled(true);
                session.save(latest);
                transaction.commit();
                return latest;
            }
        } catch (Exception e){
            throw new Exception("Error while removing reservation", e);
        }
    }

    //returns the appropriate rating object, or null if user hasn't rated the specified tour
    public Rating getRatingForTour(Tour tour) throws Exception {
        try(Session session = DatabaseHolder.getInstance().getSession()){
            Transaction transaction = session.beginTransaction();

            Query<Rating> query = session.createQuery("from Rating where tour = :tour and customer = :customer", Rating.class)
                    .setParameter("tour", tour)
                    .setParameter("customer", this);
            List<Rating> ratings = query.getResultList();

            if (ratings.size() == 0){
                transaction.commit();
                return null;
            } else if (ratings.size() == 1){
                transaction.commit();
                return ratings.get(0);
            } else {
                //query returned too many ratings
                throw new RuntimeException("Illegal database state! " + ratings.size() + " ratings exist for this customer/tour combination.");
            }
        } catch (Exception e){
            throw new Exception("Error while querying a rating", e);
        }
    }

    //adds a rating to a tour, returns the new rating object, or null if value was 0 (rating was deleted)
    public Rating rateTour(Tour tour, int value) throws Exception {
        Rating currentRating = getRatingForTour(tour);

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
                    Rating rating = new Rating(this, tour, value);
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
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
