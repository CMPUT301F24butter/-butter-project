package com.example.butter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Formats dates into a nicer format to be displayed in the app
 *
 * author: Nate Pane (natepane)
 */
public class DateFormatter {

    /**
     * Formats a date into a nicer format
     * @param inputDate
     *      should be in form 'yyyy-MM-dd'
     * @return
     *      will be in form 'MMM d, yyyy'
     */
    public String formatDate(String inputDate) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date date = format.parse(inputDate);

            SimpleDateFormat newFormat = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
            String formattedDate = newFormat.format(date);

            String dayWithSuffix = addSuffix(date);

            String finalFormat = formattedDate.replaceFirst("\\d+", dayWithSuffix);
            return  finalFormat;

        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String unformatDate(String inputDate) {
        try {
            inputDate = inputDate.replaceAll("(\\d+)(st|nd|rd|th)", "$1");

            SimpleDateFormat format = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
            Date date = format.parse(inputDate);

            SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            String unformattedDate = newFormat.format(date);

            return unformattedDate;

        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Adds a suffix to the day of the date
     * @param date
     * @return
     */
    private String addSuffix(Date date) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.ENGLISH);
        int day = Integer.parseInt(dayFormat.format(date));

        String suffix;
        if (day >= 11 && day <= 13) {
            suffix = "th";
        }
        else {
            switch (day % 10) {
                case 1: suffix = "st"; break;
                case 2: suffix = "nd"; break;
                case 3: suffix = "rd"; break;
                default: suffix = "th"; break;
            }
        }

        return day + suffix;
    }
}
