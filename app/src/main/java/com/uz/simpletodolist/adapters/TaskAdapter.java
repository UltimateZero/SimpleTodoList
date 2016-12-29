package com.uz.simpletodolist.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.uz.simpletodolist.R;
import com.uz.simpletodolist.model.Task;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by UltimateZero on 12/26/2016.
 */
public class TaskAdapter extends ArrayAdapter<Task> {
    private Context context;
    private List<Task> tasks;
    private onTaskMarkedListener taskMarkedListener;

    public TaskAdapter(Context context, List<Task> tasks, onTaskMarkedListener taskMarkedListener) {
        super(context, -1, tasks);
        this.context = context;
        this.tasks = tasks;
        this.taskMarkedListener = taskMarkedListener;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_task_item, parent, false);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.txtTitle = (TextView) rowView.findViewById(R.id.taskTitle);
            viewHolder.boxDone = (CheckBox) rowView.findViewById(R.id.boxDone);
            viewHolder.txtRowId = (TextView) rowView.findViewById(R.id.rowId);
            viewHolder.txtCreationDate = (TextView) rowView.findViewById(R.id.creationDate);
            rowView.setTag(viewHolder);

        }
        final ViewHolder holder = (ViewHolder) rowView.getTag();


        final Task task = tasks.get(position);
        holder.txtRowId.setText(position+1 + ". ");
        holder.txtTitle.setText(task.getTitle());
        holder.txtCreationDate.setText(formatCreationDate(task.getCreatedAt()));
        holder.boxDone.setChecked(task.isDone());
        holder.boxDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("task id " + task.getId() + ", checked" + holder.boxDone.isChecked());
                task.setDone(holder.boxDone.isChecked());
                if(taskMarkedListener != null) {
                    taskMarkedListener.onTaskMarked(task, task.isDone());
                }
            }
        });


        return rowView;
    }
    private static String formatCreationDate(String dateString) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy h:mm a");

        try {
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    static class ViewHolder {
        public TextView txtRowId;
        public TextView txtTitle;
        public TextView txtCreationDate;
        public CheckBox boxDone;
    }

}