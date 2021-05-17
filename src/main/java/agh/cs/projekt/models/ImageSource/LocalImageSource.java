package agh.cs.projekt.models.ImageSource;

import javax.persistence.Entity;
import java.io.IOException;
import java.net.URL;

@Entity
public class LocalImageSource extends ImageSource{

    public LocalImageSource() {
        //required by hibernate
    }

    public LocalImageSource(String path) { //path is a relative path from the resources folder e.g: "/images/my_img.png"
        super(path);
    }

    @Override
    public URL getURL() throws IOException {
        return getClass().getResource(path);
    }

}
