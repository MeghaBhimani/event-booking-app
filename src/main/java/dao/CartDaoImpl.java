package dao;

import model.CartItem;
import model.Event;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartDaoImpl implements CartDao {

    private final EventDao eventDao = new EventDaoImpl();

    @Override
    public void saveCartItems(String userId, List<CartItem> items) throws SQLException {
        // Replace all existing items for this user atomically.
        clearCart(userId);

        if (items.isEmpty()) return;

        String sql = "INSERT INTO cart_items (user_id, event_title, quantity) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (CartItem item : items) {
                stmt.setString(1, userId);
                stmt.setString(2, item.getEvent().getTitle());
                stmt.setInt(3, item.getQuantity());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    @Override
    public List<CartItem> loadCartItems(String userId) throws SQLException {
        String sql = "SELECT event_title, quantity FROM cart_items WHERE user_id = ?";
        List<CartItem> items = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String title    = rs.getString("event_title");
                    int    quantity = rs.getInt("quantity");
                    Event  event    = eventDao.getEventByTitle(title);
                    if (event != null) {
                        items.add(new CartItem(event, quantity));
                    }
                    // If the event was deleted since the cart was saved, skip it.
                }
            }
        }
        return items;
    }

    @Override
    public void clearCart(String userId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.executeUpdate();
        }
    }
}
