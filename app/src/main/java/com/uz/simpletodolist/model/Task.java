package com.uz.simpletodolist.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by UltimateZero on 12/26/2016.
 */
public class Task implements Serializable {
    private int localId = -1;
    private int id = -1;
    private String title;
    private String body;
    private boolean done;
    private String createdAt;
    private String syncedAt;
    private boolean synced;
    private boolean deleted;





    public Task() { }

    public int getLocalId() {
        return localId;
    }

    public void setLocalId(int localId) {
        this.localId = localId;
    }

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

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }


    public String getSyncedAt() {
        return syncedAt;
    }

    public void setSyncedAt(String syncedAt) {
        this.syncedAt = syncedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "Task{" +
                "localId=" + localId +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", done=" + done +
                ", createdAt='" + createdAt + '\'' +
                ", syncedAt='" + syncedAt + '\'' +
                ", synced=" + synced +
                ", deleted=" + deleted +
                '}';
    }
}
