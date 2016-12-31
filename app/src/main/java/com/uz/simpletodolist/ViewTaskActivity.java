package com.uz.simpletodolist;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.uz.simpletodolist.model.Task;

import org.w3c.dom.Text;

public class ViewTaskActivity extends AppCompatActivity {
    public static String TAG = "ViewTaskActivity";
    private static String KEY_IN_EDIT = "IN_EDIT";
    private static String KEY_TASK = "TASK";
    private static String KEY_TASK_TITLE = "TASK_TITLE";
    private static String KEY_TASK_BODY = "TASK_BODY";
    private boolean inEdit = false;
    private Task currentTask;

    FloatingActionButton btnEdit;
    EditText txtTitle;
    EditText txtBody;
    TextView txtTitleRead;
    TextView txtBodyRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtTitle = (EditText) findViewById(R.id.txtTitle);
        txtBody = (EditText) findViewById(R.id.txtBody);
        txtTitleRead = (TextView) findViewById(R.id.txtTitleRead);
        txtBodyRead = (TextView) findViewById(R.id.txtBodyRead);

        btnEdit = (FloatingActionButton) findViewById(R.id.fab);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setInEdit(true);
            }
        });

        if (savedInstanceState == null) {
            currentTask = (Task) getIntent().getSerializableExtra("TASK");
            if (currentTask == null) {
                currentTask = new Task();
                inEdit = true;
            } else {
                txtTitle.setText(currentTask.getTitle());
                txtBody.setText(currentTask.getBody());
            }

        } else {
            currentTask = (Task) savedInstanceState.getSerializable(KEY_TASK);
            inEdit = savedInstanceState.getBoolean(KEY_IN_EDIT);
            txtTitle.setText(savedInstanceState.getString(KEY_TASK_TITLE));
            txtBody.setText(savedInstanceState.getString(KEY_TASK_BODY));
        }

        txtTitleRead.setOnKeyListener(null);
        txtBodyRead.setOnKeyListener(null);

        setInEdit(inEdit);



    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_IN_EDIT, inEdit);
        outState.putSerializable(KEY_TASK, currentTask);
        outState.putString(KEY_TASK_TITLE, txtTitle.getText().toString());
        outState.putString(KEY_TASK_BODY, txtBody.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_task, menu);
        MenuItem deleteMenuItem = menu.findItem(R.id.menu_delete_task);
        if(currentTask.getLocalId() == -1) { //new

            deleteMenuItem.setEnabled(false);
        }
        else {
            deleteMenuItem.setEnabled(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_close_no_save:
                setInEdit(false);
                setResult(Activity.RESULT_CANCELED);
                finish();
                return true;
            case R.id.menu_delete_task:
                setInEdit(false);
                Intent intent = new Intent();
                intent.putExtra("TASK", currentTask);
                intent.putExtra("DELETE", true);
                setResult(Activity.RESULT_OK, intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        setInEdit(false);
        //save
        String title = txtTitle.getText().toString();
        String body = txtBody.getText().toString();
        if (title.isEmpty()) {
            title = body.split("\n")[0];
        }
        if (title.isEmpty()) {
            if (currentTask.getLocalId() == -1) { //new task
                //exit normally
                setResult(Activity.RESULT_CANCELED);
            } else {
                setInEdit(true);
                txtTitle.setError("Must enter title");
                return;
            }
        }

        else {
            if(body.isEmpty()) {
                body = title;
            }
            currentTask.setTitle(title);
            currentTask.setBody(body);
            Intent intent = new Intent();
            intent.putExtra("TASK", currentTask);
            setResult(Activity.RESULT_OK, intent);
        }

        super.onBackPressed();
    }

    private void setInEdit(boolean inEdit) {
        this.inEdit = inEdit;
        btnEdit.setVisibility(this.inEdit ? View.INVISIBLE : View.VISIBLE);
        txtTitleRead.setText(txtTitle.getText());
        txtBodyRead.setText(txtBody.getText());

        if (inEdit) {
            txtTitleRead.setVisibility(View.INVISIBLE);
            txtBodyRead.setVisibility(View.INVISIBLE);
            txtTitle.setVisibility(View.VISIBLE);
            txtBody.setVisibility(View.VISIBLE);
            txtTitle.requestFocus();
        } else {
            txtTitle.setVisibility(View.INVISIBLE);
            txtBody.setVisibility(View.INVISIBLE);
            txtTitleRead.setVisibility(View.VISIBLE);
            txtBodyRead.setVisibility(View.VISIBLE);
        }


    }


}
