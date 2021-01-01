package com.space.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class ShipUtil {

    private static final int CURRENT_YEAR = 3019;

    public static double calculateRating(double speed, Date prodDate, boolean isUsed) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(prodDate.getTime());
        double k = isUsed ? 0.5 : 1;
        return Math.round(80 * speed * k / (CURRENT_YEAR - cal.get(Calendar.YEAR) + 1) * 100) / 100.0;
    }


    public static Date getFirstDayOfYear(long date) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getDefault());
        cal.setTimeInMillis(date);
        cal.setLenient(false);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static int getYearOfMillis(long millis) {
        Date d = new Date();
        d.setTime(millis);
        return d.getYear() + 1900;
    }

}
