package cn.xxxl.chestnut.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static java.util.TimeZone.getTimeZone;

/**
 * @Description
 * @Author LeonUp
 * @Date 17-5-23.
 */
public class CUTime {

    public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd   HH:mm:ss", Locale.CHINA);
    public static final SimpleDateFormat GMT_DATE_FORMAT = new SimpleDateFormat(
            "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

    public static long getCurrentTimeLong() {
        return System.currentTimeMillis();
    }

    public static String getTimeString(long timeInMillis, SimpleDateFormat dateFormat) {
        return dateFormat.format(new Date(timeInMillis));
    }

    public static String getTimeString(long timeInMillis) {
        return getTimeString(timeInMillis, DEFAULT_DATE_FORMAT);
    }

    public static String getCurrentTimeString() {
        return getTimeString(getCurrentTimeLong());
    }

    public static String getCurrentTimeString(SimpleDateFormat dateFormat) {
        return getTimeString(getCurrentTimeLong(), dateFormat);
    }

    public static String getGMTTimeString(long timeInMillis) {
        return getGMTTimeFormat().format(new Date(timeInMillis));
    }

    public static long getGMTTimeLong(String GMTTime) throws ParseException {
        if (!CUCheck.cString(GMTTime))
            return getCurrentTimeLong();
        return getGMTTimeFormat().parse(GMTTime).getTime();
    }

    public static SimpleDateFormat getGMTTimeFormat() {
        SimpleDateFormat sdf = GMT_DATE_FORMAT;
        sdf.setTimeZone(getTimeZone("GMT"));
        return sdf;
    }
}
