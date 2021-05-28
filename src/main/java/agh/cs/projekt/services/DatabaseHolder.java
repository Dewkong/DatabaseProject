package agh.cs.projekt.services;

import agh.cs.projekt.utils.PersistentAlert;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public final class DatabaseHolder {
    private final SessionFactory sessionFactory;
    private final static DatabaseHolder INSTANCE = new DatabaseHolder();

    private DatabaseHolder() {
        Configuration config = new Configuration();
        config.configure();
        sessionFactory = config.buildSessionFactory();
    }

    public static DatabaseHolder getInstance() {
        return INSTANCE;
    }

    public Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    public void dbCallWithAlert(String title, String waitingText, String doneText, String errorText, DatabaseCall code, DatabaseErrorHandler errorHandler, DatabaseGenericHandler finalHandler) {
        PersistentAlert alert = new PersistentAlert(
                Alert.AlertType.INFORMATION,
                title,
                waitingText);
        alert.show();

        dbCallNonBlocking(
                session ->{
                    code.execute(session);
                    Platform.runLater(() -> alert.setHeaderText(doneText));
                },
                exception -> {
                    //on error
                    Platform.runLater(() -> {
                        alert.setHeaderText(errorText);
                        if (errorHandler != null) errorHandler.onError(exception);
                    });
                },
                () ->{
                    //finally
                    Platform.runLater(alert::enableClose);
                    if (finalHandler != null) finalHandler.onFinished();
                }
        );
    }

    public void dbCallWithAlert(String title, String waitingText, String doneText, String errorText, DatabaseCall code, DatabaseErrorHandler handler){
        dbCallWithAlert(title, waitingText, doneText, errorText, code, handler, null);
    }

    public void dbCallWithAlert(String title, String waitingText, String doneText, String errorText, DatabaseCall code, DatabaseGenericHandler handler){
        dbCallWithAlert(title, waitingText, doneText, errorText, code, null, handler);
    }

    public void dbCallWithAlert(String title, String waitingText, String doneText, String errorText, DatabaseCall code){
        dbCallWithAlert(title, waitingText, doneText, errorText, code, null, null);
    }

    public void dbCallNonBlocking(DatabaseCall code, DatabaseErrorHandler errorHandler, DatabaseGenericHandler finalHandler) {
        new Thread(() -> {
            try {
                dbCallBlocking(code);
            } catch (Exception e){
                e.printStackTrace();
                if (errorHandler != null) errorHandler.onError(e);
            } finally {
                if (finalHandler != null) finalHandler.onFinished();
            }
        }).start();
    }

    public void dbCallNonBlocking(DatabaseCall code, DatabaseGenericHandler handler) {
        dbCallNonBlocking(code, null, handler);
    }

    public void dbCallNonBlocking(DatabaseCall code, DatabaseErrorHandler handler) {
        dbCallNonBlocking(code, handler, null);
    }

    public void dbCallNonBlocking(DatabaseCall code) {
        dbCallNonBlocking(code, null, null);
    }

    public void dbCallBlocking(DatabaseCall code) throws Exception {
        try (Session session = getSession()) {
            code.execute(session);
        }
    }

    public interface DatabaseCall {
        void execute(Session session) throws Exception;
    }

    public interface DatabaseErrorHandler {
        void onError(Exception e);
    }

    public interface DatabaseGenericHandler {
        void onFinished();
    }

}
