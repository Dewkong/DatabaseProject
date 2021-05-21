package agh.cs.projekt.models;

import agh.cs.projekt.services.DatabaseHolder;
import agh.cs.projekt.models.ImageSource.ImageSource;
import javafx.util.Pair;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.persistence.*;
import java.sql.Date;
import java.util.List;

@Entity
public class Tour {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CountryEnum country;

    @Column(nullable = false)
    private Date tourDate;

    private int maxPlaces;

    private float price;

    @Column(nullable = false)
    private String description;

    @ManyToOne
    private ImageSource image;

    Tour(){
        //required by Hibernate
    }

    public Tour(String name, CountryEnum country, Date date, int maxPlaces, float price, String description, ImageSource image) {
        this.name = name;
        this.country = country;
        this.tourDate = date;
        this.maxPlaces = maxPlaces;
        this.price = price;
        this.description = description;
        this.image = image;
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

    public CountryEnum getCountry() {
        return country;
    }

    public void setCountry(CountryEnum country) {
        this.country = country;
    }

    public Date getTourDate() {
        return tourDate;
    }

    public void setTourDate(Date tourDate) {
        this.tourDate = tourDate;
    }

    public int getMaxPlaces() {
        return maxPlaces;
    }

    public void setMaxPlaces(int maxPlaces) {
        this.maxPlaces = maxPlaces;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ImageSource getImage() {
        return image;
    }

    public void setImage(ImageSource image) {
        this.image = image;
    }

    public int getAvailablePlaces(){ //returns a negative number on error
        try(Session session = DatabaseHolder.getInstance().getSession()) {
            Transaction transaction = session.beginTransaction();

            Query<Reservation> query = session.createQuery("from Reservation where tour = :tour", Reservation.class).setParameter("tour", this);
            List<Reservation> reservations = query.getResultList();
            int reservedPlaces = 0;
            for (Reservation r : reservations){
                reservedPlaces += r.getReservedAmount();
            }

            transaction.commit();
            return maxPlaces - reservedPlaces;
        } catch (Exception e){
            System.err.println("Error when fetching number of places");
            e.printStackTrace();
            return -1;
        }
    }

    //returns a pair of (average rating, total ratings), or null in case of error
    public Pair<Double, Long> getRating(){
        try(Session session = DatabaseHolder.getInstance().getSession()) {
            Transaction transaction = session.beginTransaction();

            Long ratingsAmt = session.createQuery("select count(*) from Rating where tour = :tour", Long.class).setParameter("tour", this).getSingleResult();
            Double average = session.createQuery("select avg(rating) from Rating where tour = :tour", Double.class).setParameter("tour", this).getSingleResult();
            if (ratingsAmt == null) ratingsAmt = -1L;
            if (average == null) average = 0.0;

            transaction.commit();
            return new Pair<>(average, ratingsAmt);
        } catch (Exception e){
            System.err.println("Error when fetching number of places");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "Tour{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", country=" + country +
                ", tourDate=" + tourDate +
                ", maxPlaces=" + maxPlaces +
                ", price=" + price +
                ", description='" + description + '\'' +
                '}';
    }
}
