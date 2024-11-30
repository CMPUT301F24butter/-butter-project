package com.example.butter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DateFormatterTest {

    @Test
    public void testInvalidFormat() {
        String invalidDateFormat = "12-01-2024";

        DateFormatter dateFormatter = new DateFormatter();
        String formattedDate = dateFormatter.formatDate(invalidDateFormat);

        assertNotEquals(formattedDate, "Dec 1st, 2024");
    }

    @Test
    public void testStSuffix() {
        String invalidDateFormat = "2024-12-01";

        DateFormatter dateFormatter = new DateFormatter();
        String formattedDate = dateFormatter.formatDate(invalidDateFormat);

        assertEquals(formattedDate, "Dec 1st, 2024");
    }

    @Test
    public void testNdSuffix() {
        String invalidDateFormat = "2024-12-02";

        DateFormatter dateFormatter = new DateFormatter();
        String formattedDate = dateFormatter.formatDate(invalidDateFormat);

        assertEquals(formattedDate, "Dec 2nd, 2024");
    }

    @Test
    public void testRdSuffix() {
        String invalidDateFormat = "2024-12-03";

        DateFormatter dateFormatter = new DateFormatter();
        String formattedDate = dateFormatter.formatDate(invalidDateFormat);

        assertEquals(formattedDate, "Dec 3rd, 2024");
    }

    @Test
    public void testThSuffix() {
        String invalidDateFormat = "2024-12-04";

        DateFormatter dateFormatter = new DateFormatter();
        String formattedDate = dateFormatter.formatDate(invalidDateFormat);

        assertEquals(formattedDate, "Dec 4th, 2024");
    }

    @Test
    public void testUnformat() {
        String invalidDateFormat = "December 1st, 2024";

        DateFormatter dateFormatter = new DateFormatter();
        String formattedDate = dateFormatter.unformatDate(invalidDateFormat);

        assertEquals(formattedDate, "2024-12-01");
    }
}
