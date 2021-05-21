package agh.cs.projekt.services;

import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Stack;

public class NavigationService {

    private final static NavigationService INSTANCE = new NavigationService();
    private final Stack<SceneFactory<?>> history = new Stack<>();
    private Parent currentSceneRoot;

    public static NavigationService getInstance() {
        return INSTANCE;
    }

    //sets up the initial scene based on the primaryStage, and the .fxml filename of the scene to be loaded
    //(optional) a lambda taking in some FXMLController, which will be called after the view is initialised
    public <T> void setInitialScene(Stage primaryStage, String fxmlFileName)  {
        setInitialScene(primaryStage, fxmlFileName, null);
    }
    public <T> void setInitialScene(Stage primaryStage, String fxmlFileName, SceneInitializer<T> sceneInitializer)  {
        if (!history.empty()){
            throw new RuntimeException("Attempting to set initial scene, when one was already set before");
        }

        Scene newScene = new Scene(new Group()); //dummy scene, will be replaced with the proper root
        currentSceneRoot = newScene.getRoot();
        SceneFactory<T> factory = new SceneFactory<>(fxmlFileName, sceneInitializer);
        try {
            setSceneRoot(factory);
            history.add(factory);
            primaryStage.setScene(newScene);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //move to a new scene based on the the .fxml filename of the scene to be loaded
    //(optional) a lambda taking in some FXMLController, which will be called after the view is initialised
    public <T> void setScene(String fxmlFileName) {
        setScene(fxmlFileName, null);
    }
    public <T> void setScene(String fxmlFileName, SceneInitializer<T> sceneInitializer) {
        SceneFactory<T> factory = new SceneFactory<>(fxmlFileName, sceneInitializer);
        try {
            setSceneRoot(factory);
            history.add(factory);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    //private helper function, swaps the root of the current scene with the root initialized in SceneFactory<someController>
    private <T> void setSceneRoot(SceneFactory<T> factory) throws IOException {
        Parent root = factory.getInitializedRoot();
        currentSceneRoot.getScene().setRoot(root);
        currentSceneRoot = root;
    }

    //returns to a previous scene
    //if that scene was initialized by an optional lambda, that lambda will be called again
    public void goBack() {
        if (history.empty()){
            throw new RuntimeException("Called goBack but no scenes were displayed yet, or history was flushed");
        }
        history.pop();
        if (history.empty()){
            throw new RuntimeException("Called goBack but there was no scene to go back to");
        }
        SceneFactory<?> previous = history.peek();
        try {
            setSceneRoot(previous);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //destroys the scene transition history (prevents from calling goBack)
    public void flushHistory(){
        while (!history.empty()){
            history.pop();
        }
    }

    //private inner class tasked with loading and initializing scenes
    private static class SceneFactory<T>{

        private final SceneInitializer<T> initializer;
        private final URL resourceURL;

        public SceneFactory(String sceneFXML) throws IllegalArgumentException {
            this(sceneFXML, null);
        }

        public SceneFactory(String sceneFXML, SceneInitializer<T> initializer) throws IllegalArgumentException {
            resourceURL = getClass().getResource("/fxml/" + sceneFXML);
            if (resourceURL == null) {
                throw new IllegalArgumentException("Couldn't find .fxml file in resources/fxml/" + sceneFXML);
            }
            this.initializer = initializer;
        }

        public Parent getInitializedRoot() throws IOException{
            FXMLLoader loader = new FXMLLoader(resourceURL);
            try {
                Parent root = loader.load();
                if (initializer != null) {
                    T controller = loader.getController();
                    initializer.initialize(controller);
                }
                return root;
            } catch (IOException e) {
                throw new IOException("Error when loading FXML", e);
            }
        }

    }

    //interface to be used as a scene initializer lambda
    public interface SceneInitializer<T>{
        void initialize(T fxmlController);
    }

}
