package io.mo.viaport.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * 关于时间操作的工具类
 * @author xdsjs, wizos
 * @date 2015/10/14
 */
public class TimeUtils {



    /**
     * 将 时间戳 转为指定的 格式
     * @param pattern 要转为的格式（例如 yyyy-MM-dd HH:mm:ss）
     * @param timestamp 时间戳（毫秒）
     * @return 格式化的时间
     */
    public static String format(String pattern, long timestamp) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        return dateFormat.format(new Date(timestamp));
    }
    public static String format(String pattern, long timestamp, Locale locale) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, locale);
        return dateFormat.format(new Date(timestamp));
    }

    // public static String humanized(long timestamp) {
    //     return DateUtils.getRelativeTimeSpanString(
    //             timestamp, System.currentTimeMillis(),
    //             DateUtils.MINUTE_IN_MILLIS,
    //             DateUtils.FORMAT_ABBREV_ALL
    //     ).toString();
    // }


    /**
     * 格式化时长，将整数型的时长转为更可读的形式
     * @param duration 时间间隔
     * @return 可读性更强的格式
     */
    public static StringBuilder readableDuration(int duration) {
        if (duration < 0) {
            duration = 0;
        }
        int cache = duration / 1000;
        int second = cache % 60;
        cache = cache / 60;
        int minute = cache % 60;
        int hour = cache / 60;
        StringBuilder formatDuration = new StringBuilder();
        if (hour > 0) {
            formatDuration.append(hour);
            formatDuration.append(":");
        }
        if (minute < 10) {
            formatDuration.append("0");
        }
        formatDuration.append(minute);
        formatDuration.append(":");

        if (second < 10) {
            formatDuration.append("0");
        }
        formatDuration.append(second);
        return formatDuration;
    }
}