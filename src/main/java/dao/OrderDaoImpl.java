package dao;

import model.CartItem;
import model.Order;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OrderDaoImpl implements OrderDao {

    private static final DateTimeFormatter DB_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void insertOrder(String orderNumber, List<CartItem> items,
                            double totalPrice, String userId) throws SQLException {
        String sql = "INSERT INTO orders "
                + "(order_number, timestamp, event_title, seats, total_price, user_id)"
                + " VALUES (?, ?, ?, ?, ?, ?)";

        String timestamp = LocalDateTime.now().format(DB_FORMAT);

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (CartItem item : items) {
                stmt.setString(1, orderNumber);
                stmt.setString(2, timestamp);
                stmt.setString(3, item.getEvent().getTitle());
                stmt.setInt(4, item.getQuantity());
                // item.getPrice() = quantity × unit price (correct line total)
                stmt.setDouble(5, item.getPrice());
                stmt.setString(6, userId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    @Override
    public int getLastOrderNumber() throws SQLException {
        String sql = "SELECT MAX(CAST(order_number AS INTEGER)) AS max_order FROM orders";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("max_order"); // returns 0 if the table is empty
            }
        }
        return 0;
    }

    @Override
    public List<Order> getOrdersForUser(String userId) throws SQLException {
        String sql = "SELECT order_number, timestamp, event_title, seats, total_price"
                + " FROM orders WHERE user_id = ?"
                + " ORDER BY timestamp DESC";
        List<Order> orders = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRow(rs, null));
                }
            }
        }
        return orders;
    }

    @Override
    public List<Order> getAllOrders() throws SQLException {
        String sql = "SELECT order_number, timestamp, event_title, seats, total_price, user_id"
                + " FROM orders ORDER BY timestamp DESC";
        List<Order> orders = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String userId = rs.getString("user_id");
                orders.add(mapRow(rs, userId));
            }
        }
        return orders;
    }

    private Order mapRow(ResultSet rs, String userId) throws SQLException {
        String        orderNumber  = rs.getString("order_number");
        String        timestampStr = rs.getString("timestamp");
        LocalDateTime timestamp    = LocalDateTime.parse(timestampStr, DB_FORMAT);
        String        eventTitle   = rs.getString("event_title");
        int           seats        = rs.getInt("seats");
        double        price        = rs.getDouble("total_price");
        return new Order(orderNumber, timestamp, eventTitle, seats, price, userId);
    }
}
