package com.uz.simpletodolist.model;

import java.io.Serializable;

/**
 * Created by UltimateZero on 12/31/2016.
 */

public class User implements Serializable {
    private int id;
    private String email;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
