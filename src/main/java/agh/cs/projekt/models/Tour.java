package agh.cs.projekt.models;

import agh.cs.projekt.models.ImageSource.ImageSource;
import agh.cs.projekt.services.DatabaseHolder;
import javafx.util.Pair;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;

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

    @Lob
    @Column(nullable = false)
    private String description;

    @ManyToOne(cascade = CascadeType.ALL)
    private ImageSource image;

    public Tour(){
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
            int result = getAvailablePlaces(session);
            transaction.commit();
            return result;
        } catch (Exception e){
            System.err.println("Error when fetching number of places");
            e.printStackTrace();
            return -1;
        }
    }

    public int getAvailablePlaces(Session session) { //returns a negative number on error
        try {
            BigDecimal result = (BigDecimal) session.createNativeQuery("select AVAILABLE_PLACES(:id) from dual").setParameter("id", this.id).getSingleResult();
            System.out.println("Result " + result);
            return result.intValue();
        } catch (NoResultException e){
            return -1;
        }
    }

    //returns a pair of (average rating, total ratings), or null in case of error
    public Pair<Double, Long> getRating(){
        try(Session session = DatabaseHolder.getInstance().getSession()) {
            Transaction transaction = session.beginTransaction();

            BigDecimal ratingsAmt = (BigDecimal) session.createNativeQuery("select RATINGS_AMT(:id) from dual").setParameter("id", this.id).getSingleResult();
            BigDecimal average = (BigDecimal) session.createNativeQuery("select AVG_RATING(:id) from dual").setParameter("id", this.id).getSingleResult();
            if (ratingsAmt == null) ratingsAmt = BigDecimal.valueOf(-1L);
            if (average == null) average = BigDecimal.valueOf(0.0);

            transaction.commit();
            return new Pair<>(average.doubleValue(), ratingsAmt.longValue());
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
