package com.kupangstudio.shoufangbao.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间转换相关工具
 *
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-8-24
 */
public class TimeUtils {

    public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat DATE_FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat DATE_FORMAT_HOUR = new SimpleDateFormat("HH:mm");
    public static final SimpleDateFormat DATE_FORMAT_MINUTE = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static final SimpleDateFormat DATE_FORMAT_MONTH = new SimpleDateFormat("yyyy-MM");

    private TimeUtils() {
        throw new AssertionError();
    }

    /**
     * long time to string
     *
     * @param timeInMillis
     * @param dateFormat
     * @return
     */
    public static String getTime(long timeInMillis, SimpleDateFormat dateFormat) {
        return dateFormat.format(new Date(timeInMillis));
    }

    /**
     * long time to string, format is {@link #DEFAULT_DATE_FORMAT}
     *
     * @param timeInMillis
     * @return
     */
    public static String getTime(long timeInMillis) {
        return getTime(timeInMillis, DEFAULT_DATE_FORMAT);
    }

    /**
     * get current time in milliseconds
     *
     * @return
     */
    public static long getCurrentTimeInLong() {
        return System.currentTimeMillis();
    }

    /**
     * get current time in milliseconds, format is {@link #DEFAULT_DATE_FORMAT}
     *
     * @return
     */
    public static String getCurrentTimeInString() {
        return getTime(getCurrentTimeInLong());
    }

    /**
     * get current time in milliseconds
     *
     * @return
     */
    public static String getCurrentTimeInString(SimpleDateFormat dateFormat) {
        return getTime(getCurrentTimeInLong(), dateFormat);
    }

    public static String getCustomFollowData(long time) {
       return getTime(time, TimeUtils.DATE_FORMAT_MINUTE);
    }

    public static String getCustomReportData(long time) {
        return getTime(time, TimeUtils.DATE_FORMAT_DATE);
    }

    public static String getCustomReportHour(long time) {
        return getTime(time, TimeUtils.DATE_FORMAT_HOUR);
    }

    public static String getPhoneReceiverData(long time) {
        return getTime(time, TimeUtils.DATE_FORMAT_MONTH);
    }
}
