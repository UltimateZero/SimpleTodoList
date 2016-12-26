package com.uz.simpletodolist.adapters;

import android.content.Context;
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

import java.util.List;

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
            rowView.setTag(viewHolder);

        }
        final ViewHolder holder = (ViewHolder) rowView.getTag();


        final Task task = tasks.get(position);
        holder.txtRowId.setText(position+1 + ". ");
        holder.txtTitle.setText(task.getTitle());
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

    static class ViewHolder {
        public TextView txtRowId;
        public TextView txtTitle;
        public CheckBox boxDone;
    }

}