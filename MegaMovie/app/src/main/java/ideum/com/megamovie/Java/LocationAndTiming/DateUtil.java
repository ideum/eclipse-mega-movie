package ideum.com.megamovie.Java.LocationAndTiming;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by MT_User on 7/19/2017.
 */

public class DateUtil {

    public static String countdownStringToDate(Date targetDate) {
        Calendar c = Calendar.getInstance();
        Long currentMills = c.getTimeInMillis();
        Long targetTimeMills = targetDate.getTime();
        Long interval = targetTimeMills - currentMills;

        Long hours = TimeUnit.MILLISECONDS.toHours(interval);

        interval = interval - TimeUnit.HOURS.toMillis(hours);

        long minutes = TimeUnit.MILLISECONDS.toMinutes(interval);
        String hourUnit = "hours";
        if (hours == 1) {
            hourUnit = "hour";
        }

        String minuteUnit = "minutes";
        if (minutes == 1) {
            minuteUnit = "minute";
        }


        return String.valueOf(hours) + " " + hourUnit + ", " + String.valueOf(minutes) + " " + minuteUnit;

    }

    public static String countdownDaysString(Long mills) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(mills);
        DateFormat formatter = new SimpleDateFormat("DD", Locale.US);

        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(c.getTime());
    }

    public static String countdownHoursString(Long mills) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(mills);
        DateFormat formatter = new SimpleDateFormat("HH", Locale.US);

        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(c.getTime());
    }

    public static String countdownMinutesString(Long mills) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(mills);
        DateFormat formatter = new SimpleDateFormat("mm", Locale.US);

        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(c.getTime());
    }


    public static String countdownSecondsString(Long mills) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(mills);
        DateFormat formatter = new SimpleDateFormat("ss", Locale.US);

        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(c.getTime());
    }

}
