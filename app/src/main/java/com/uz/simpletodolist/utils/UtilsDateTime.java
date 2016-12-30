package com.uz.simpletodolist.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by UltimateZero on 12/30/2016.
 */

public class UtilsDateTime {
    public static String getISO8601String() {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return inputFormat.format(new Date());
    }

}
