package com.mabsplace.mabsplaceback.utils;

import com.mabsplace.mabsplaceback.domain.enums.Period;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class Utils {
    public static String generateUniqueName(String entityId, String extension) {
        String uuid = UUID.randomUUID().toString();
        return entityId + "-" + uuid + "." + extension;
    }

    public static String generateUniqueName2(long entityId, String entityName) {
        return entityId + "-" + entityName + "-" + "image";
    }

    public static Date addPeriod(Date date, Period period) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        switch (period) {
            case MONTHLY:
                calendar.add(Calendar.MONTH, 1);
                break;
            case QUARTERLY:
                calendar.add(Calendar.MONTH, 3);
                break;
            case SEMI_ANNUALLY:
                calendar.add(Calendar.MONTH, 6);
                break;
            case YEARLY:
                calendar.add(Calendar.YEAR, 1);
                break;
            case DAILY:
                calendar.add(Calendar.DATE, 1);
                break;
        }

        return calendar.getTime();
    }

    public static String generateUniquePromoCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static Date addDays(Date endDate, int additionalDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);
        calendar.add(Calendar.DATE, additionalDays);
        return calendar.getTime();
    }

    public static Date getCurrentDate() {
        return new Date();
    }
}