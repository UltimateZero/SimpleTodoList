package com.uz.simpletodolist.core;

/**
 * Created by UltimateZero on 12/26/2016.
 */
public class UnauthorizedUserException extends Exception {
    public UnauthorizedUserException(String detailMessage) {
        super(detailMessage);
    }
}
