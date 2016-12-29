package com.uz.simpletodolist;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class ViewTaskActivity extends AppCompatActivity {
    public static String TAG = "ViewTaskActivity";
    private static String KEY_IN_EDIT = "IN_EDIT";
    private static String KEY_TASK_TITLE  = "TASK_TITLE";
    private static String KEY_TASK_BODY = "TASK_BODY";
    private boolean inEdit = false;

    FloatingActionButton btnEdit;
    EditText txtTitle;
    EditText txtBody;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txtTitle = (EditText) findViewById(R.id.txtTitle);
        txtBody = (EditText) findViewById(R.id.txtBody);

        btnEdit = (FloatingActionButton) findViewById(R.id.fab);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setInEdit(true);
            }
        });

        if(savedInstanceState == null) {
            txtTitle.setText(getIntent().getStringExtra(KEY_TASK_TITLE));
            txtBody.setText(getIntent().getStringExtra(KEY_TASK_BODY));
        }
        else {
            inEdit = savedInstanceState.getBoolean(KEY_IN_EDIT);
            txtTitle.setText(savedInstanceState.getString(KEY_TASK_TITLE));
            txtBody.setText(savedInstanceState.getString(KEY_TASK_BODY));
        }
        setInEdit(inEdit);


    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_IN_EDIT, inEdit);
        outState.putString(KEY_TASK_TITLE, txtTitle.getText().toString());
        outState.putString(KEY_TASK_BODY, txtBody.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        setInEdit(false);
        //save
        super.onBackPressed();
    }

    private void setInEdit(boolean inEdit) {
        this.inEdit = inEdit;
        btnEdit.setVisibility(this.inEdit ? View.INVISIBLE : View.VISIBLE);
        txtTitle.setInputType(inEdit ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_NULL);
        txtBody.setInputType(inEdit ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_NULL);
        txtTitle.setTextIsSelectable(true);
        txtBody.setTextIsSelectable(true);
        txtTitle.requestFocus();
    }


}
