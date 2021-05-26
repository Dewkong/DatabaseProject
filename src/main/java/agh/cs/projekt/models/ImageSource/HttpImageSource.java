package agh.cs.projekt.models.ImageSource;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import javax.persistence.Entity;
import java.io.IOException;
import java.net.URL;

@Entity
public class HttpImageSource extends ImageSource {

    public HttpImageSource() {
        //required by hibernate
    }

    public HttpImageSource(String path) { //path is a "http:..." url
        this.path = path;
    }

    @Override
    public String getName() throws IOException {
        return new URL(path).toString();
    }

    @Override
    public Image getImage() throws IOException{
        return SwingFXUtils.toFXImage(ImageIO.read(new URL(path)), null);
    }



}
