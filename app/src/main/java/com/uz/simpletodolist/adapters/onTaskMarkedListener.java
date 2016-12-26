package com.uz.simpletodolist.adapters;

import com.uz.simpletodolist.model.Task;

/**
 * Created by UltimateZero on 12/26/2016.
 */
public abstract class onTaskMarkedListener {

    public abstract void onTaskMarked(Task task, boolean done);
}
