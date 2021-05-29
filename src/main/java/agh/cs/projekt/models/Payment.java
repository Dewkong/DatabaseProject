package agh.cs.projekt.models;

import javax.persistence.*;
import java.sql.Date;

@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne
    private Reservation reservation;

    @Column(nullable = false)
    private Date paymentDate;

    private float amount;

    public Payment() {
        //required by Hibernate
    }

    public Payment(Customer customer, Reservation reservation, Date paymentDate, float amount) {
        this.reservation = reservation;
        this.paymentDate = paymentDate;
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", reservation=" + reservation +
                ", paymentDate=" + paymentDate +
                ", amount=" + amount +
                '}';
    }

}
