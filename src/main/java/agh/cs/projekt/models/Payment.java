package agh.cs.projekt.models;

import javax.persistence.*;
import java.sql.Date;

@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne
    @JoinColumn(nullable=false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(nullable=false)
    private Reservation reservation;

    @Column(nullable = false)
    private Date paymentDate;

    private float amount;

    public Payment() {
        //required by Hibernate
    }

    public Payment(Customer customer, Reservation reservation, Date paymentDate, float amount) {
        this.customer = customer;
        this.reservation = reservation;
        this.paymentDate = paymentDate;
        this.amount = amount;
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
                ", customer=" + customer.getId() +
                ", reservation=" + reservation.getId() +
                ", paymentDate=" + paymentDate +
                ", amount=" + amount +
                '}';
    }

}
