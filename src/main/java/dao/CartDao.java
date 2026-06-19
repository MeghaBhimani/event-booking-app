package dao;

import model.CartItem;

import java.sql.SQLException;
import java.util.List;

public interface CartDao {

    void saveCartItems(String userId, List<CartItem> items) throws SQLException;

    List<CartItem> loadCartItems(String userId) throws SQLException;

    void clearCart(String userId) throws SQLException;
}
