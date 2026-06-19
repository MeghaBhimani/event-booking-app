package dao;

import model.CartItem;
import model.Order;

import java.sql.SQLException;
import java.util.List;

public interface OrderDao {

    void insertOrder(String orderNumber, List<CartItem> items,
                     double totalPrice, String userId) throws SQLException;

    int getLastOrderNumber() throws SQLException;

    List<Order> getOrdersForUser(String userId) throws SQLException;

    List<Order> getAllOrders() throws SQLException;
}
