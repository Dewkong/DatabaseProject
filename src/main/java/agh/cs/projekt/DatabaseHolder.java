package agh.cs.projekt;

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
}
