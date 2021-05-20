package agh.cs.projekt.models;

import javax.persistence.*;

@Entity
public class ApplicationUser {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @OneToOne
    @JoinColumn(nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private String login;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String salt;

    @Column(nullable = false)
    private RoleEnum role;

    public ApplicationUser(){
        //required by Hibernate
    }

    public ApplicationUser(Customer customer, String login, String password, String salt, RoleEnum role) {
        this.customer = customer;
        this.login = login;
        this.password = password;
        this.salt = salt;
        this.role = role;
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

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public RoleEnum getRole() {
        return role;
    }

    public void setRole(RoleEnum role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", customer=" + customer.getId() +
                ", login=" + login +
                ", password=" + password +
                ", salt=" + salt +
                ", role=" + role +
                "}";
    }

}
