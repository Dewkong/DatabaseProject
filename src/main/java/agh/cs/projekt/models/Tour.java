package agh.cs.projekt.models;

import agh.cs.projekt.DatabaseHolder;
import agh.cs.projekt.models.ImageSource.ImageSource;
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
