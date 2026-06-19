package model;

public class CartItem {

    private final Event event;
    private int quantity;

    public CartItem(Event event, int quantity) {
        this.event = event;
        this.quantity = quantity;
    }

    public Event getEvent() { return event; }

    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return (double) quantity * event.getPrice(); }

    public String getTitle()  { return event.getTitle(); }

    public String getDay()    { return event.getDay(); }

    public String getVenue()  { return event.getVenue(); }
}
