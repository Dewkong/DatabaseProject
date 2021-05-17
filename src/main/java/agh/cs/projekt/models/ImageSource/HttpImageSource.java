package agh.cs.projekt.models.ImageSource;

import javax.persistence.Entity;
import java.io.IOException;
import java.net.URL;

@Entity
public class HttpImageSource extends ImageSource {

    public HttpImageSource() {
        //required by hibernate
    }

    public HttpImageSource(String path) { //path is a "http:..." url
        super(path);
    }

    @Override
    public URL getURL() throws IOException {
        return new URL(path);
    }
}
