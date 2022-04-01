package be.cytomine.utils;

import be.cytomine.domain.CytomineDomain;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static SimpleDateFormat MONGO_DB_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public static SimpleDateFormat REPORT_FILENAME_FORMAT = new SimpleDateFormat("yyyyMMdd_hhmmss");

    public static String getTimeToString(Date date) {
        if (date == null) {
            return null;
        } else {
            return String.valueOf(date.getTime());
        }
    }

    public static String getLocaleDate(Date date){
        DateFormat DFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.getDefault());
        return DFormat.format(date);
    }

    public static String getSimpleFormatLocaleDate(Date date){
        return REPORT_FILENAME_FORMAT.format(date);
    }

    public static Long computeDateInMillis(Date created) {
        return created != null ? created.getTime() - new Date(0).getTime() : null;
    }

    public static Long computeDateInMillis(Long l) {
        return l != null ? l - new Date(0).getTime() : null;
    }

    public static Date computeMillisInDate(long millis) {return new Date(millis);}

    public static Comparator<CytomineDomain> descCreatedComparator() {
        return (lhs, rhs) -> {
            if (lhs==null) {
                return 1;
            }
            if (rhs==null) {
                return -1;
            }
            if (lhs.getCreated().getTime() < rhs.getCreated().getTime())
                return 1;
            else if (lhs.getCreated().getTime() == rhs.getCreated().getTime())
                return 0;
            else
                return -1;
        };
    }
}
