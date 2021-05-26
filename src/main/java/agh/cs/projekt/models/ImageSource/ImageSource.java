package agh.cs.projekt.models.ImageSource;

import javafx.scene.image.Image;

import javax.persistence.*;
import java.io.IOException;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ImageSource {

    @Id
    @Column(nullable = false)
    protected String path;

    public ImageSource() {
        //required by hibernate
    }

    public abstract String getName() throws IOException;
    public abstract Image getImage() throws IOException;

}
