package agh.cs.projekt.models.ImageSource;

import javax.persistence.*;
import java.io.IOException;
import java.net.URL;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ImageSource {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private int id;

    @Column(nullable = false)
    protected String path;

    public ImageSource() {
        //required by hibernate
    }

    public ImageSource(String path) {
        this.path = path;
    }

    public abstract URL getURL() throws IOException;

}
