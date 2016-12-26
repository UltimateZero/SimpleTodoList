package com.uz.simpletodolist.model;

import java.util.Date;

/**
 * Created by UltimateZero on 12/26/2016.
 */
public class Task {
    private int id;
    private String title;
    private String body;
    private int userId;
    private boolean done;
    private String createdAt;

    public Task() { }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }


    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", userId=" + userId +
                ", done=" + done +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
