package dao;

import model.CartItem;
import model.Event;
import model.EventGroup;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class EventDaoImpl implements EventDao {

    private static final String TABLE = "events";

    @Override
    public Event getEvent(String title, String venue, String day) throws SQLException {
        String sql = "SELECT * FROM " + TABLE
                + " WHERE event_name = ? AND venue = ? AND day = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, venue);
            stmt.setString(3, day);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Event getEventByTitle(String title) throws SQLException {
        String sql = "SELECT * FROM " + TABLE + " WHERE event_name = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
        // Note: SQLException propagates to the caller — it is not swallowed here.
    }

    @Override
    public List<Event> getAllEvents() throws SQLException {
        String sql = "SELECT * FROM " + TABLE + " WHERE enabled = TRUE";
        return queryEvents(sql);
    }

    @Override
    public List<Event> getAllEventsIncludingDisabled() throws SQLException {
        String sql = "SELECT * FROM " + TABLE;
        return queryEvents(sql);
    }

    @Override
    public List<EventGroup> getGroupedEvents() throws SQLException {
        List<Event> all = getAllEventsIncludingDisabled();

        // Group by title, preserving insertion order so the tree is stable.
        Map<String, List<Event>> grouped = all.stream()
                .collect(Collectors.groupingBy(Event::getTitle,
                        LinkedHashMap::new, Collectors.toList()));

        List<EventGroup> result = new ArrayList<>();
        for (Map.Entry<String, List<Event>> entry : grouped.entrySet()) {
            result.add(new EventGroup(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    @Override
    public Event createEvent(String title, String venue, String day,
                             Integer price, Integer sold, Integer total,
                             Boolean enabled) throws SQLException {
        String sql = "INSERT INTO " + TABLE
                + " (event_name, venue, day, price, sold, total, enabled)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, venue);
            stmt.setString(3, day);
            stmt.setInt(4, price);
            stmt.setInt(5, sold);
            stmt.setInt(6, total);
            stmt.setBoolean(7, enabled != null ? enabled : true);
            stmt.executeUpdate();
            return new Event(title, venue, day, price, sold, total,
                    enabled != null ? enabled : true);
        }
    }

    @Override
    public void updateEvent(String title, String venue, String day,
                            Integer price, Integer sold, Integer total) throws SQLException {
        String sql = "UPDATE " + TABLE
                + " SET price = ?, sold = ?, total = ?"
                + " WHERE event_name = ? AND venue = ? AND day = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, price);
            stmt.setInt(2, sold);
            stmt.setInt(3, total);
            stmt.setString(4, title);
            stmt.setString(5, venue);
            stmt.setString(6, day);
            stmt.executeUpdate();
        }
    }

    @Override
    public void updateEvent(String oldTitle, String oldVenue, String oldDay,
                            String newVenue, String newDay,
                            Integer price, Integer sold, Integer total) throws SQLException {
        String sql = "UPDATE " + TABLE
                + " SET venue = ?, day = ?, price = ?, sold = ?, total = ?"
                + " WHERE event_name = ? AND venue = ? AND day = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newVenue);
            stmt.setString(2, newDay);
            stmt.setInt(3, price);
            stmt.setInt(4, sold);
            stmt.setInt(5, total);
            stmt.setString(6, oldTitle);
            stmt.setString(7, oldVenue);
            stmt.setString(8, oldDay);
            stmt.executeUpdate();
        }
    }

    @Override
    public void setEventEnabled(String title, String venue, String day,
                                boolean enabled) throws SQLException {
        String sql = "UPDATE " + TABLE
                + " SET enabled = ? WHERE event_name = ? AND venue = ? AND day = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, enabled);
            stmt.setString(2, title);
            stmt.setString(3, venue);
            stmt.setString(4, day);
            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteEvent(String title, String venue, String day) throws SQLException {
        String sql = "DELETE FROM " + TABLE
                + " WHERE event_name = ? AND venue = ? AND day = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, venue);
            stmt.setString(3, day);
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException(
                        "Delete failed: no event found for ("
                                + title + ", " + venue + ", " + day + ").");
            }
        }
    }

    @Override
    public void processCheckout(Collection<CartItem> cartItems) throws SQLException {
        String sql = "UPDATE " + TABLE
                + " SET sold = sold + ?"
                + " WHERE event_name = ? AND (total - sold) >= ?";

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false); // begin explicit transaction
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (CartItem item : cartItems) {
                    stmt.setInt(1, item.getQuantity());
                    stmt.setString(2, item.getEvent().getTitle());
                    stmt.setInt(3, item.getQuantity());
                    int updated = stmt.executeUpdate();
                    if (updated == 0) {
                        conn.rollback();
                        throw new SQLException(
                                "Not enough seats available for: "
                                        + item.getEvent().getTitle());
                    }
                }
                conn.commit(); // all seat updates succeeded
            } catch (SQLException e) {
                conn.rollback(); // undo any partial updates
                throw e;
            }
        }
    }

    private List<Event> queryEvents(String sql) throws SQLException {
        List<Event> events = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                events.add(mapRow(rs));
            }
        }
        return events;
    }

    private Event mapRow(ResultSet rs) throws SQLException {
        return new Event(
                rs.getString("event_name"),
                rs.getString("venue"),
                rs.getString("day"),
                rs.getInt("price"),
                rs.getInt("sold"),
                rs.getInt("total"),
                rs.getBoolean("enabled")
        );
    }
}
