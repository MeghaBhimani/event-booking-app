package model;

import java.util.List;

public class EventGroup {

    private String       title;
    private List<Event>  variations;

    public EventGroup(String title, List<Event> variations) {
        this.title      = title;
        this.variations = variations;
    }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public List<Event> getVariations() { return variations; }
}
