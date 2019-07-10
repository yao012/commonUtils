package com.eques.eqhome.commons.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 使用DateTimeFormatter类代替SimpleDateFormat
 * 注意:
 * 这里有坑出没!!!
 * toLocalDateTime:返回值中带有时分秒,
 * toLocalDate是不带有时分秒的,
 * 所以在格式化成带时分秒格式时是会抛出异常的
 *
 * @author : zhenguo.yao
 * @date : 2019/4/16 0016 13:26
 */
public class DateUtils {

    /**
     * 按照 yyyy-MM-dd 的格式解析时间
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * yyyy-MM-dd HH:mm:ss
     */
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 本系统使用的时区id
     */
    private static final String OFFSET_ID = "+08:00";

    /**
     * 格式:yyyy-MM-dd 2019-04-16
     */
    public static String formatCurrentDate() {
        return LocalDate.now(ZoneId.of(OFFSET_ID)).format(DATE_FORMATTER);
    }

    /**
     * 格式:yyyy-MM-dd 2019-04-16
     */
    public static String formatSpecifiedDate(long timestamp) {
        return Instant
                .ofEpochMilli(timestamp)
                .atZone(ZoneId.of(OFFSET_ID))
                .toLocalDate()
                .format(DATE_FORMATTER);
    }

    /**
     * 格式:yyyy-MM-dd 2019-04-16
     */
    public static String formatSpecifiedDate(Date date) {
        return formatDate(date)
                .toInstant()
                .atZone(ZoneId.of(OFFSET_ID))
                .toLocalDate()
                .format(DATE_FORMATTER);
    }

    /**
     * yyyy-MM-dd HH:mm:ss
     */
    public static String formatCurrentTime() {
        return LocalDateTime.now(ZoneId.of(OFFSET_ID)).format(DATETIME_FORMATTER);
    }

    /**
     * yyyy-MM-dd HH:mm:ss
     */
    public static String formatSpecifiedTime(long timestamp) {
        return Instant
                .ofEpochMilli(timestamp)
                .atZone(ZoneId.of(OFFSET_ID))
                .toLocalDateTime()
                .format(DATETIME_FORMATTER);
    }

    /**
     * 格式:yyyy-MM-dd HH:mm:ss
     */
    public static String formatSpecifiedTime(Date date) {
        return formatDate(date)
                .toInstant()
                .atZone(ZoneId.of(OFFSET_ID))
                .toLocalDateTime()
                .format(DATETIME_FORMATTER);
    }

    public static Date parseTime(String timeStr) {
        LocalDateTime localDateTime = LocalDateTime.parse(timeStr, DATETIME_FORMATTER);
        return Date.from(localDateTime.toInstant(ZoneOffset.of(OFFSET_ID)));
    }

    private static Date formatDate(Date date){
        if(date instanceof java.sql.Date){
            return new Date(date.getTime());
        }
        return date;
    }

    public static void main(String[] args) {
        System.out.println("formatCurrentDate()  = "+formatCurrentDate());
        System.out.println("formatSpecifiedDate(long) = "+formatSpecifiedDate(System.currentTimeMillis()));
        System.out.println("formatSpecifiedDate(Date)   = "+formatSpecifiedDate(new Date()));
        System.out.println("formatCurrentTime() = "+formatCurrentTime());
        System.out.println("formatSpecifiedTime(long)  = "+formatSpecifiedTime(System.currentTimeMillis()));
        System.out.println("formatSpecifiedTime(Date) = "+formatSpecifiedTime(new Date()));
        System.out.println("parseTime(String) = "+parseTime("2019-07-10 17:00:00"));
    }

}
