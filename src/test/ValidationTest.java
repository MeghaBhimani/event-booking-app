import model.Event;
import org.junit.jupiter.api.Test;
import Test.Validation;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValidationTest {

    @Test
    void testIsNotEmpty() {
        assertTrue(Validation.isNotEmpty("Event"));
        assertFalse(Validation.isNotEmpty(""));
        assertFalse(Validation.isNotEmpty("   "));
    }

    @Test
    void testIsNumeric() {
        assertTrue(Validation.isNumeric("123"));
        assertFalse(Validation.isNumeric("abc"));
    }

    @Test
    void testIsValidPrice() {
        assertTrue(Validation.isValidPrice("0"));
        assertTrue(Validation.isValidPrice("100"));
        assertFalse(Validation.isValidPrice("-10"));
        assertFalse(Validation.isValidPrice("ten"));
    }

    @Test
    void testIsValidCapacity() {
        assertTrue(Validation.isValidCapacity("10"));
        assertFalse(Validation.isValidCapacity("0"));
        assertFalse(Validation.isValidCapacity("-1"));
    }

    @Test
    void testIsDuplicateEvent() {
        Event e1 = new Event("Music Fest", "Main Hall", "Monday", 100, 0, 100, true);
        Event e2 = new Event("Art Show", "Gallery", "Tuesday", 80, 0, 50, true);
        List<Event> existingEvents = Arrays.asList(e1, e2);

        assertTrue(Validation.isDuplicateEvent("Music Fest", "Main Hall", "Monday", existingEvents));
        assertFalse(Validation.isDuplicateEvent("Dance Night", "Club", "Friday", existingEvents));
    }

    @Test
    void testIsValidConfirmationCode() {
        assertTrue(Validation.isValidConfirmationCode("123456"));
        assertFalse(Validation.isValidConfirmationCode("12345"));
        assertFalse(Validation.isValidConfirmationCode("1234567"));
        assertFalse(Validation.isValidConfirmationCode("abc123"));
        assertFalse(Validation.isValidConfirmationCode(null));
    }

    @Test
    void testIsEventDayValid() {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        List<String> days = List.of("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN");
        int todayIndex = today.getValue() - 1;

        for (int i = todayIndex; i < days.size(); i++) {
            assertTrue(Validation.isEventDayValid(days.get(i)));
        }

        for (int i = 0; i < todayIndex; i++) {
            assertFalse(Validation.isEventDayValid(days.get(i)));
        }
    }

}
