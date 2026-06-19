package dao;

import model.User;

import java.sql.SQLException;

public interface UserDao {

    void setup() throws SQLException;

    User getUser(String username, String encryptedPassword) throws SQLException;

    User createUser(String username, String encryptedPassword,
                    String preferredName) throws SQLException;

    void updatePassword(String username, String encryptedPassword) throws SQLException;
}
