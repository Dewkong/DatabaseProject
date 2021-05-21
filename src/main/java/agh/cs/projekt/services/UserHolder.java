package agh.cs.projekt.services;

import agh.cs.projekt.models.ApplicationUser;

public final class UserHolder {
    private ApplicationUser applicationUser;
    private final static UserHolder INSTANCE = new UserHolder();

    private UserHolder() {}

    public static UserHolder getInstance() {
        return INSTANCE;
    }

    public void setUser(ApplicationUser applicationUser) {
        this.applicationUser = applicationUser;
    }

    public ApplicationUser getUser() {
        return this.applicationUser;
    }

    public void removeUser() {
        this.applicationUser = null;
    }

    public boolean isPresent() {
        return this.applicationUser != null;
    }
}
