package agh.cs.projekt.models;

import agh.cs.projekt.services.DatabaseHolder;
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

    public Reservation getReservationForTour(Tour tour){
        try(Session session = DatabaseHolder.getInstance().getSession()) {
            Transaction transaction = session.beginTransaction();

            Reservation reservation = session.createQuery("from Reservation where tour = :tour and customer = :customer", Reservation.class)
                    .setParameter("tour", tour)
                    .setParameter("customer", this).getSingleResult();

            transaction.commit();
            return reservation;
        }
        catch (NoResultException e){
            //consume exception
            return null;
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
