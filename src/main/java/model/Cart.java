package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cart {

    private static Cart instance;

    private final Map<String, CartItem> items = new HashMap<>();

    private Cart() {}

    public static synchronized Cart getInstance() {
        if (instance == null) {
            instance = new Cart();
        }
        return instance;
    }

    public boolean addOrUpdateEvent(Event event, int quantity) {
        CartItem existing = items.get(event.getTitle());
        int currentQty = (existing == null) ? 0 : existing.getQuantity();

        if (quantity + currentQty > event.getAvailableSeats()) {
            return false;
        }

        if (existing == null) {
            items.put(event.getTitle(), new CartItem(event, quantity));
        } else {
            existing.setQuantity(currentQty + quantity);
        }
        return true;
    }

    public void removeItem(String eventTitle) {
        items.remove(eventTitle);
    }

    public void clear() {
        items.clear();
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items.values());
    }

    public boolean canAddQuantity(Event event, int quantityToAdd) {
        CartItem existing = items.get(event.getTitle());
        int currentQty = (existing == null) ? 0 : existing.getQuantity();
        return (currentQty + quantityToAdd) <= event.getAvailableSeats();
    }

    public void setItems(List<CartItem> savedItems) {
        items.clear();
        for (CartItem item : savedItems) {
            items.put(item.getEvent().getTitle(), item);
        }
    }
}
