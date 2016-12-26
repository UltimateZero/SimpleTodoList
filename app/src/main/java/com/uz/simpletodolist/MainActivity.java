package com.uz.simpletodolist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.uz.simpletodolist.core.ConnectionManager;
import com.uz.simpletodolist.model.Task;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    ListView listTasks;
    Button btnAdd;
    Button btnRefresh;

    ArrayList<Task> tasks;
    ArrayAdapter<Task> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ConnectionManager.setContext(getApplicationContext());


        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnRefresh = (Button) findViewById(R.id.btnRefresh);
        listTasks = (ListView) findViewById(R.id.listTasks);


        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTask("bla bla", "body test");
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTasks();
            }
        });

        tasks = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tasks);
        listTasks.setAdapter(adapter);

        login();
    }


    private void loggedIn() {
        getTasks();

    }

    private void updateTask(Task task) {
        ConnectionManager.getInstance().updateTask(task, new Response.Listener<Task>() {
            @Override
            public void onResponse(Task response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
    }

    private void deleteTask(int id) {
        ConnectionManager.getInstance().deleteTask(id, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse != null && error.networkResponse.statusCode == 404) {
                    return;
                }
                error.printStackTrace();

            }
        });
    }

    private void markTask(int id, boolean done) {
        ConnectionManager.getInstance().markTask(id, done, new Response.Listener<Task>() {
            @Override
            public void onResponse(Task response) {
                Log.d(TAG, response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
    }

    private void addTask(String title, String body) {
        ConnectionManager.getInstance().addTask(title, body, new Response.Listener<Task>() {
            @Override
            public void onResponse(Task response) {
                Log.d(TAG, response.toString());
                MainActivity.this.adapter.add(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
    }

    private void getTasks() {
        ConnectionManager.getInstance().getTasks(new Response.Listener<List<Task>>() {
            @Override
            public void onResponse(List<Task> tasks) {
                MainActivity.this.adapter.clear();
                for (Task task : tasks) {
                    Log.d(TAG, task.toString());
                    MainActivity.this.adapter.add(task);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
    }

    private void login() {
        ConnectionManager.getInstance().login("test123@gmail.com", "testtest", new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(MainActivity.this, "Logged in!", Toast.LENGTH_SHORT).show();
                        loggedIn();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(error.networkResponse != null && error.networkResponse.statusCode == 401) {
                            Toast.makeText(MainActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}
