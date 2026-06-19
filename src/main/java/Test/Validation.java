package Test;

import model.Event;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class Validation {

    private Validation() {}

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isNumeric(String value) {
        if (value == null || value.isEmpty()) return false;
        return value.chars().allMatch(Character::isDigit);
    }

    public static boolean isValidPrice(String priceStr) {
        try {
            return Integer.parseInt(priceStr.trim()) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidCapacity(String capacityStr) {
        try {
            return Integer.parseInt(capacityStr.trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDuplicateEvent(String title, String venue, String day,
                                           List<Event> existingEvents) {
        for (Event e : existingEvents) {
            if (e.getTitle().equalsIgnoreCase(title)
                    && e.getVenue().equalsIgnoreCase(venue)
                    && e.getDay().equalsIgnoreCase(day)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidConfirmationCode(String code) {
        if (code == null) return false;
        return code.matches("\\d{6}");
    }

    public static boolean isEventDayValid(String day) {
        if (day == null || day.trim().isEmpty()) return false;

        // Map three-letter abbreviation to ISO DayOfWeek (Mon=1 … Sun=7).
        DayOfWeek eventDay = parseDayOfWeek(day.trim());
        if (eventDay == null) return false;

        DayOfWeek today = LocalDate.now().getDayOfWeek();
        // DayOfWeek.getValue() returns 1 (Mon) through 7 (Sun).
        return eventDay.getValue() >= today.getValue();
    }

    private static DayOfWeek parseDayOfWeek(String day) {
        switch (day.toLowerCase()) {
            case "mon": return DayOfWeek.MONDAY;
            case "tue": return DayOfWeek.TUESDAY;
            case "wed": return DayOfWeek.WEDNESDAY;
            case "thu": return DayOfWeek.THURSDAY;
            case "fri": return DayOfWeek.FRIDAY;
            case "sat": return DayOfWeek.SATURDAY;
            case "sun": return DayOfWeek.SUNDAY;
            default:    return null;
        }
    }
}
