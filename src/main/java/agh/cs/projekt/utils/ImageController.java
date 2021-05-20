package agh.cs.projekt.utils;

import agh.cs.projekt.models.ImageSource.ImageSource;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageController {

    static Map<URL, Image> loadedImages = new HashMap<>();
    static Map<URL, List<ImageView>> waitingViews = new HashMap<>();
    static Image error_img = null;
    static Image loading_img = null;

    public static void init(ImageSource error_img_src, ImageSource loading_image_src){
        try {
            error_img = SwingFXUtils.toFXImage(ImageIO.read(error_img_src.getURL()), null);
        } catch (IOException e) {
            System.err.println("Failed to retrieve error image");
            e.printStackTrace();
            error_img = null;
        }

        try {
            loading_img = SwingFXUtils.toFXImage(ImageIO.read(loading_image_src.getURL()), null);
        } catch (IOException e) {
            System.err.println("Failed to retrieve loading image");
            e.printStackTrace();
            loading_img = null;
        }
    }

    //adds a view to the waiting list for a given url, returns the list of waiting views
    //if view is null just returns the waiting views without adding anything
    private static synchronized List<ImageView> addAndGetWaiting(URL url, ImageView view){
        if (view != null && loadedImages.containsKey(url) && loadedImages.get(url) != null){
            //image has already been loaded, don't wait
            view.setImage(loadedImages.get(url));
        } else if (view != null) {
            //add view to waiting list
            if (!waitingViews.containsKey(url)) waitingViews.put(url, new ArrayList<>());
            waitingViews.get(url).add(view);
        }
        return waitingViews.get(url);
    }

    //not intended to be called in a multithreaded context
    //this function will offload its work to a worker thread by itself
    public static void loadFromSource(ImageView imageView, ImageSource src){
        if (src == null) {
            imageView.setImage(error_img);
            centerImage(imageView);
            return;
        }

        URL url;
        try {
            url = src.getURL();
        } catch (IOException e) {
            System.out.println("Invalid ImageSource URL");
            e.printStackTrace();
            imageView.setImage(error_img);
            centerImage(imageView);
            return;
        }

        if (loadedImages.containsKey(url)){
            Image i = loadedImages.get(url);
            if (i == null) {
                //image is being loaded
                imageView.setImage(loading_img);
                centerImage(imageView);
                addAndGetWaiting(url, imageView);
            } else {
                //image has already been buffered
                imageView.setImage(i);
                centerImage(imageView);
            }
        } else {
            loadedImages.put(url, null); //indicate we are buffering the image
            imageView.setImage(loading_img);
            centerImage(imageView);
            addAndGetWaiting(url, imageView);
            loadImage(url);
        }
    }

    //creates a new thread which reads and caches an image
    private static void loadImage(URL url){
        new Thread(()->{
            Image i;
            try {
                i = SwingFXUtils.toFXImage(ImageIO.read(url), null);
            } catch (Exception e) {
                System.err.println("Failed to load ImageSource");
                e.printStackTrace();
                i = error_img;
            }
            loadedImages.put(url, i); //add image to loaded images map

            //notify waiting views
            List<ImageView> waitingList = addAndGetWaiting(url, null);
            waitingViews.remove(url);
            if (waitingList != null){
                for (ImageView view : waitingList){
                    view.setImage(i);
                    centerImage(view);
                }
            }
        }).start();
    }

    //centers the image present in an ImageView
    public static void centerImage(ImageView imageView){
        Image img = imageView.getImage();
        if (img != null) {
//            double ratioX = imageView.getFitWidth() / img.getWidth();
//            double ratioY = imageView.getFitHeight() / img.getHeight();
//
//            double reduction = Math.min(ratioX, ratioY);
//
//            double wOffset = img.getWidth() * reduction;
//            double hOffset = img.getHeight() * reduction;
//
//            double xMin = (imageView.getFitWidth() - wOffset) / 2.0;
//            double yMin = (imageView.getFitHeight() - hOffset) / 2.0;
//
//            Rectangle2D viewport = new Rectangle2D(xMin, yMin, imageView.getFitWidth(), imageView.getFitHeight());
//            imageView.setViewport(viewport);
        }
    }

}
