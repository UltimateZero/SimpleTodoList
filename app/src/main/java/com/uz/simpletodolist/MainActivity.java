package com.uz.simpletodolist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.uz.simpletodolist.adapters.TaskAdapter;
import com.uz.simpletodolist.adapters.onTaskMarkedListener;
import com.uz.simpletodolist.core.ConnectionManager;
import com.uz.simpletodolist.core.DatabaseManager;
import com.uz.simpletodolist.model.Task;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private static final String TASKS_ADAPTER_KEY = "TaskAdapterKey";

    SwipeRefreshLayout swipeRefreshLayout;
    ListView listTasks;


    ArrayList<Task> tasks;
    TaskAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConnectionManager.setContext(getApplicationContext());

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTasks();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTask();
            }
        });


        listTasks = (ListView) findViewById(R.id.listTasks);

        Task dummy = new Task();
        dummy.setTitle("test title");
        dummy.setBody("test desc");
        //DatabaseManager.getInstance(this).insertTask(dummy);


        registerForContextMenu(listTasks);
        listTasks.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return true;
            }
        });
        listTasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editTask(adapter.getItem(position));
            }
        });

        if(savedInstanceState == null) {
            tasks = new ArrayList<>();
        }
        else {
            tasks = (ArrayList<Task>) savedInstanceState.getSerializable(TASKS_ADAPTER_KEY);
        }
        adapter = new TaskAdapter(this, tasks, new onTaskMarkedListener() {
            @Override
            public void onTaskMarked(Task task, boolean done) {
                if(done)
                    markTask(task);
                else
                    updateTask(task);
            }
        });
        listTasks.setAdapter(adapter);

        if(savedInstanceState == null)
            login();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_refresh:
                swipeRefreshLayout.setRefreshing(true);
                getTasks();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void loggedIn() {
        getTasks();

    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void addTask() {
        editTask(null);
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

    private void deleteTask(Task task) {
        adapter.remove(task);
        deleteTask(task.getId());
    }
    private void deleteTask(int id) {
        ConnectionManager.getInstance().deleteTask(id, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                    return;
                }
                error.printStackTrace();

            }
        });
    }

    private void markTask(Task task) {
        ConnectionManager.getInstance().markTask(task.getId(), new Response.Listener<Task>() {
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
                MainActivity.this.swipeRefreshLayout.setRefreshing(false);
               //MainActivity.this.showToast("Refreshed");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                MainActivity.this.swipeRefreshLayout.setRefreshing(false);
                MainActivity.this.showToast("Error occurred");
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
                        if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                            Toast.makeText(MainActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void editTask(Task task) {
        Intent intent = new Intent();
        intent.setClass(this, ViewTaskActivity.class);
        intent.putExtra("TASK", task);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId()==R.id.listTasks) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle("Id: " + (adapter.getItem(info.position)).getId());
            menu.add(Menu.NONE, 0, 0, "Delete");
            menu.add(Menu.NONE, 1, 0, "Edit");

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Activity.RESULT_CANCELED || data == null) return;
        Task receivedTask = (Task) data.getSerializableExtra("TASK");
        Task ownTask = adapter.findTaskById(receivedTask.getId());
        if(ownTask != null) {
            if(data.getBooleanExtra("DELETE", false)) {
                deleteTask(ownTask.getId());
                adapter.remove(ownTask);
                showToast("Task deleted");
                return;
            }
            if(ownTask.getTitle().equals(receivedTask.getTitle())
                    && ownTask.getBody().equals(receivedTask.getBody())) return;
            ownTask.setTitle(receivedTask.getTitle());
            ownTask.setBody(receivedTask.getBody());
            updateTask(ownTask);
            adapter.notifyDataSetChanged();
        }
        else {
            addTask(receivedTask.getTitle(), receivedTask.getBody());
        }
        showToast("Saved");


    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        Task task = (adapter.getItem(info.position));
        if(menuItemIndex == 0) {
            deleteTask(task);
        }
        else if(menuItemIndex == 1) {
            editTask(task);
        }

        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(TASKS_ADAPTER_KEY, tasks);
        super.onSaveInstanceState(outState);
    }
}
