package dao;

import model.User;

import java.sql.*;

public class UserDaoImpl implements UserDao {

    private static final String TABLE_NAME = "users";

    @Override
    public void setup() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + "username     TEXT NOT NULL,"
                + "password     TEXT NOT NULL,"
                + "preferredName TEXT NOT NULL,"
                + "PRIMARY KEY (username)"
                + ")";
        try (Connection conn = Database.getConnection();
             Statement  stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    @Override
    public User getUser(String username, String encryptedPassword) throws SQLException {
        String sql = "SELECT * FROM " + TABLE_NAME
                + " WHERE username = ? AND password = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, encryptedPassword);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setPreferredName(rs.getString("preferredName"));
                    return user;
                }
            }
        }
        return null;
    }

    @Override
    public User createUser(String username, String encryptedPassword,
                           String preferredName) throws SQLException {
        String sql = "INSERT INTO " + TABLE_NAME + " VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, encryptedPassword);
            stmt.setString(3, preferredName);
            stmt.executeUpdate();
            return new User(username, encryptedPassword, preferredName);
        }
    }

    @Override
    public void updatePassword(String username, String encryptedPassword) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME
                + " SET password = ? WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, encryptedPassword);
            stmt.setString(2, username);
            stmt.executeUpdate();
        }
    }
}
