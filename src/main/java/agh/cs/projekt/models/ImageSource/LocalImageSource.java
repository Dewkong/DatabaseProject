package agh.cs.projekt.models.ImageSource;

import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

@Entity
public class LocalImageSource extends ImageSource{

    @Lob
    @Column(nullable=false, columnDefinition="BLOB")
    protected byte[] image;

    public LocalImageSource() {
        //required by hibernate
    }

    public LocalImageSource(String path) { //path is an absolute path to the image (but now that the UI handles that, you should not worry about it)
        try {
            File file = new File(path);
            BufferedImage bImage = ImageIO.read(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(bImage, "jpg", bos);
            this.image = bos.toByteArray();
            this.path = path;
        } catch (IOException e){
            System.err.println("Error when reading image");
            e.printStackTrace();
        }
    }

    @Override
    public String getName() throws IOException{
        return path;
    }

    @Override
    public Image getImage() throws IOException{
        return new Image(new ByteArrayInputStream(this.image));
    }

}
