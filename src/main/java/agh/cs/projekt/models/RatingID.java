package agh.cs.projekt.models;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class RatingID implements Serializable {
    private int customerID;
    private int tourID;
}
