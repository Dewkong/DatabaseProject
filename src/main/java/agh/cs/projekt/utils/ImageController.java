package agh.cs.projekt.utils;

import agh.cs.projekt.models.ImageSource.ImageSource;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageController {

    static Map<String, Image> loadedImages = new HashMap<>();
    static Map<String, List<ImageView>> waitingViews = new HashMap<>();
    static Image error_img = null;
    static Image loading_img = null;

    public static void init(ImageSource error_img_src, ImageSource loading_img_src){
        try {
            error_img = error_img_src.getImage();
        } catch (IOException e) {
            System.err.println("Failed to retrieve error image");
            e.printStackTrace();
            error_img = null;
        }

        try {
            loading_img = loading_img_src.getImage();
        } catch (IOException e) {
            System.err.println("Failed to retrieve loading image");
            e.printStackTrace();
            loading_img = null;
        }
    }

    //adds a view to the waiting list for a given name, returns the list of waiting views
    //if view is null just returns the waiting views without adding anything
    private static synchronized List<ImageView> addAndGetWaiting(String name, ImageView view){
        if (view != null && loadedImages.containsKey(name) && loadedImages.get(name) != null){
            //image has already been loaded, don't wait
            view.setImage(loadedImages.get(name));
        } else if (view != null) {
            //add view to waiting list
            if (!waitingViews.containsKey(name)) waitingViews.put(name, new ArrayList<>());
            waitingViews.get(name).add(view);
        }
        return waitingViews.get(name);
    }

    //not intended to be called in a multithreaded context
    //this function will offload its work to a worker thread by itself
    public static void loadFromSource(ImageView imageView, ImageSource src){
        if (src == null) {
            imageView.setImage(error_img);
            return;
        }

        String name;
        try {
            name = src.getName();
        } catch (IOException e) {
            System.out.println("Invalid ImageSource URL");
            e.printStackTrace();
            imageView.setImage(error_img);
            return;
        }

        if (loadedImages.containsKey(name)){
            Image i = loadedImages.get(name);
            if (i == null) {
                //image is being loaded
                imageView.setImage(loading_img);
                addAndGetWaiting(name, imageView);
            } else {
                //image has already been buffered
                imageView.setImage(i);
            }
        } else {
            loadedImages.put(name, null); //indicate we are buffering the image
            imageView.setImage(loading_img);
            addAndGetWaiting(name, imageView);
            loadImage(name, src);
        }
    }

    //creates a new thread which reads and caches an image
    private static void loadImage(String name, ImageSource src){
        new Thread(()->{
            Image i;
            try {
                i = src.getImage();
            } catch (Exception e) {
                System.err.println("Failed to load ImageSource");
                e.printStackTrace();
                i = error_img;
            }
            loadedImages.put(name, i); //add image to loaded images map

            //notify waiting views
            List<ImageView> waitingList = addAndGetWaiting(name, null);
            waitingViews.remove(name);
            if (waitingList != null){
                for (ImageView view : waitingList){
                    view.setImage(i);
                }
            }
        }).start();
    }


}
