package model;

import dao.CartDao;
import dao.CartDaoImpl;
import dao.EventDao;
import dao.EventDaoImpl;
import dao.UserDao;
import dao.UserDaoImpl;

import java.sql.SQLException;

public class Model {

    private final UserDao  userDao;
    private final EventDao eventDao;
    private final CartDao  cartDao;

    private User currentUser;

    public Model() {
        userDao  = new UserDaoImpl();
        eventDao = new EventDaoImpl();
        cartDao  = new CartDaoImpl();
    }

    public void setup() throws SQLException {
        userDao.setup();
    }

    public UserDao getUserDao() { return userDao; }

    public EventDao getEventDao() { return eventDao; }

    public CartDao getCartDao() { return cartDao; }

    public User getCurrentUser() { return currentUser; }

    public void setCurrentUser(User user) { this.currentUser = user; }

    public String getCurrentUserId() {
        return (currentUser != null) ? currentUser.getUsername() : null;
    }
}
