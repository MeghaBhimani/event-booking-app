package model;

import dao.EventDao;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Event {

    private String  title;
    private String  venue;
    private String  day;
    private int     price;
    private int     ticketsSold;
    private int     totalTickets;
    private boolean enabled;

    public Event() {}

    public Event(String title, String venue, String day,
                 int price, int ticketsSold, int totalTickets) {
        this(title, venue, day, price, ticketsSold, totalTickets, true);
    }

    public Event(String title, String venue, String day,
                 int price, int ticketsSold, int totalTickets, boolean enabled) {
        this.title        = title;
        this.venue        = venue;
        this.day          = day;
        this.price        = price;
        this.ticketsSold  = ticketsSold;
        this.totalTickets = totalTickets;
        this.enabled      = enabled;
    }

    public String  getTitle()        { return title; }
    public String  getVenue()        { return venue; }
    public String  getDay()          { return day; }
    public int     getPrice()        { return price; }
    public int     getTicketsSold()  { return ticketsSold; }
    public int     getTotalTickets() { return totalTickets; }
    public boolean isEnabled()       { return enabled; }

    public void setTitle(String title)              { this.title        = title; }
    public void setVenue(String venue)              { this.venue        = venue; }
    public void setDay(String day)                  { this.day          = day; }
    public void setPrice(int price)                 { this.price        = price; }
    public void setTicketsSold(int ticketsSold)     { this.ticketsSold  = ticketsSold; }
    public void setTotalTickets(int totalTickets)   { this.totalTickets = totalTickets; }
    public void setEnabled(boolean enabled)         { this.enabled      = enabled; }

    public int getAvailableSeats() {
        return totalTickets - ticketsSold;
    }

    public static void addToDatabase(EventDao eventDao) throws Exception {
        List<Event> fileEvents = readEventsFromFile();
        for (Event event : fileEvents) {
            Event existing = eventDao.getEvent(
                    event.getTitle(), event.getVenue(), event.getDay());
            if (existing == null) {
                eventDao.createEvent(
                        event.getTitle(), event.getVenue(), event.getDay(),
                        event.getPrice(), event.getTicketsSold(),
                        event.getTotalTickets(), true);
            } else {
                // Preserve DB sold count and enabled flag; only refresh
                // price and capacity from the file.
                eventDao.updateEvent(
                        event.getTitle(), event.getVenue(), event.getDay(),
                        event.getPrice(), existing.getTicketsSold(),
                        event.getTotalTickets());
            }
        }
    }

    public static void writeEventsToFile(EventDao eventDao) throws Exception {
        List<Event> dbEvents = eventDao.getAllEvents();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("events.dat"))) {
            for (Event event : dbEvents) {
                writer.write(String.format("%s;%s;%s;%d;%d;%d%n",
                        event.getTitle(),
                        event.getVenue(),
                        event.getDay(),
                        event.getPrice(),
                        event.getTicketsSold(),
                        event.getTotalTickets()));
            }
        }
    }

    private static List<Event> readEventsFromFile() throws IOException {
        List<Event> events = new ArrayList<>();
        File file = new File("events.dat");
        if (!file.exists()) {
            return events;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length != 6) continue; // skip malformed lines
                try {
                    String title       = parts[0].trim();
                    String venue       = parts[1].trim();
                    String day         = parts[2].trim();
                    int    price       = Integer.parseInt(parts[3].trim());
                    int    ticketsSold = Integer.parseInt(parts[4].trim());
                    int    total       = Integer.parseInt(parts[5].trim());
                    events.add(new Event(title, venue, day, price, ticketsSold, total));
                } catch (NumberFormatException e) {
                    // Skip lines with non-numeric price / ticket fields.
                    System.err.println("Skipping malformed line in events.dat: " + line);
                }
            }
        }
        return events;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        Event other = (Event) o;
        return Objects.equals(title, other.title)
                && Objects.equals(venue, other.venue)
                && Objects.equals(day,   other.day);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, venue, day);
    }

    @Override
    public String toString() {
        return String.format("Event{title='%s', venue='%s', day='%s', price=%d, sold=%d/%d, enabled=%b}",
                title, venue, day, price, ticketsSold, totalTickets, enabled);
    }
}
