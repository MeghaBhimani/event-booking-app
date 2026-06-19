package dao;

import model.CartItem;
import model.Event;
import model.EventGroup;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public interface EventDao {

    Event getEvent(String title, String venue, String day) throws SQLException;

    Event getEventByTitle(String title) throws SQLException;

    List<Event> getAllEvents() throws SQLException;

    List<Event> getAllEventsIncludingDisabled() throws SQLException;

    List<EventGroup> getGroupedEvents() throws SQLException;

    Event createEvent(String title, String venue, String day,
                      Integer price, Integer sold, Integer total,
                      Boolean enabled) throws SQLException;

    void updateEvent(String title, String venue, String day,
                     Integer price, Integer sold, Integer total) throws SQLException;

    void updateEvent(String oldTitle, String oldVenue, String oldDay,
                     String newVenue, String newDay,
                     Integer price, Integer sold, Integer total) throws SQLException;

    void setEventEnabled(String title, String venue, String day,
                         boolean enabled) throws SQLException;

    void deleteEvent(String title, String venue, String day) throws SQLException;

    void processCheckout(Collection<CartItem> cartItems) throws SQLException;
}
