package com.uz.simpletodolist;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.uz.simpletodolist.adapters.TaskAdapter;
import com.uz.simpletodolist.adapters.onTaskMarkedListener;
import com.uz.simpletodolist.core.AsyncVolleyRequest;
import com.uz.simpletodolist.core.ConnectionManager;
import com.uz.simpletodolist.core.DatabaseManager;
import com.uz.simpletodolist.model.Task;
import com.uz.simpletodolist.model.User;
import com.uz.simpletodolist.utils.UtilsDateTime;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private static final String TASKS_ADAPTER_KEY = "TaskAdapterKey";
    private static final String USER_KEY = "USER";
    public static final int EDIT_TASK_REQUEST_CODE = 4;

    private User currentUser;

    SwipeRefreshLayout swipeRefreshLayout;
    ListView listTasks;


    TaskAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getTasks();
                    }
                }).start();

            }
        });

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                        addNewTask();
            }
        });


        listTasks = (ListView) findViewById(R.id.listTasks);
        final TextView emptyTextView = (TextView) findViewById(R.id.empty);
        listTasks.setEmptyView(emptyTextView);


        registerForContextMenu(listTasks);

        listTasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editTask(adapter.getItem(position));
            }
        });

        ArrayList<Task> tasksList;
        if(savedInstanceState == null) {
            tasksList = new ArrayList<>();
            currentUser = (User) getIntent().getSerializableExtra(USER_KEY);
        }
        else {
            tasksList = (ArrayList<Task>) savedInstanceState.getSerializable(TASKS_ADAPTER_KEY);
            currentUser = (User) savedInstanceState.getSerializable(USER_KEY);
        }
        adapter = new TaskAdapter(this, tasksList, new onTaskMarkedListener() {
            @Override
            public void onTaskMarked(final Task task, final boolean done) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(done)
                            markTask(task);
                        else
                            updateTask(task);
                    }
                }).start();
            }
        });
        listTasks.setAdapter(adapter);

        if(savedInstanceState == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getTasksDB();
                }
            }).start();
        }


    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        View empty = findViewById(R.id.empty);
        ListView list = (ListView) findViewById(R.id.listTasks);
        list.setEmptyView(empty);
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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getTasks();
                    }
                }).start();
                break;
            case R.id.menu_logout:
                ConnectionManager.getInstance(getApplicationContext()).logout();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra("LOGGEDOUT", true);
                finish();
                startActivity(intent);
                break;
            case R.id.menu_deletedone:
                new AlertDialog.Builder(this)
                        .setTitle("Delete all completed tasks")
                        .setMessage("Are you sure you want to delete ALL completed tasks?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        deleteAllDoneTasks();
                                        showToast("Deleted all completed tasks");
                                    }
                                }).start();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId()==R.id.listTasks) {
            menu.setHeaderTitle("Options");
            menu.add(Menu.NONE, 0, 0, "Delete");
            menu.add(Menu.NONE, 1, 0, "Edit");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        final Task task = (adapter.getItem(info.position));
        if(menuItemIndex == 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    deleteTask(task);
                }
            }).start();

        }
        else if(menuItemIndex == 1) {
            editTask(task);

        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_CANCELED) return;
        if(requestCode == EDIT_TASK_REQUEST_CODE) {
            final Task receivedTask = (Task) data.getSerializableExtra("TASK");
            final boolean deleted = data.getBooleanExtra("DELETE", false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Task ownTask = adapter.findTaskById(receivedTask.getLocalId());
                    if(ownTask != null) {
                        if(deleted) {
                            deleteTask(ownTask);

                            showToast("Task deleted");
                            return;
                        }
                        if(ownTask.getTitle().equals(receivedTask.getTitle())
                                && ownTask.getBody().equals(receivedTask.getBody())) return;
                        ownTask.setTitle(receivedTask.getTitle());
                        ownTask.setBody(receivedTask.getBody());
                        updateTask(ownTask);

                    }
                    else {
                        addTask(receivedTask.getTitle(), receivedTask.getBody());
                    }
                    showToast("Saved");
                }
            }).start();

        }



    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(TASKS_ADAPTER_KEY, adapter.getList());
        outState.putSerializable(USER_KEY, currentUser);
        super.onSaveInstanceState(outState);
    }

    private void adapterAddTask(final Task task) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.add(task);
            }
        });
    }

    private void adapterRemoveTask(final Task task) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.remove(task);
            }
        });
    }

    private void adapterNotifySetChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void deleteAllDoneTasks() {
        ArrayList<Task> tasks = (ArrayList<Task>)adapter.getList().clone(); //clone it so we can delete from original
        for (Task task : tasks) {
            if(task.isDone())
                deleteTask(task);
        }
    }

    private void addNewTask() {
        editTask(null);
    }

    private void updateTask(final Task task) {
        task.setSynced(false);
        DatabaseManager.getInstance(getApplicationContext()).updateTask(task);
        adapterNotifySetChanged();
        updateTaskRemote(task);
    }

    private void updateTaskRemote(final Task task) {
        ConnectionManager.getInstance(getApplicationContext()).updateTask(task, new AsyncVolleyRequest<Task>() {
            @Override
            public void onResponse(Task response) {
                task.setSynced(true);
                task.setSyncedAt(UtilsDateTime.getISO8601String());
                DatabaseManager.getInstance(getApplicationContext()).updateTask(task);
                adapterNotifySetChanged();
            }

            @Override
            public void onError(VolleyError error) {
                showToast("Error");
            }
        });
    }

    private void deleteTask(final Task task) {
        DatabaseManager.getInstance(getApplicationContext()).deleteTask(task);
        adapterRemoveTask(task);
       

        deleteTaskRemote(task);
    }

    private void deleteTaskRemote(final Task task) {

        ConnectionManager.getInstance().deleteTask(task.getId(), new AsyncVolleyRequest<String>() {
            @Override
            public void onResponse(String response) {
            }

            @Override
            public void onError(VolleyError error) {

            }
        });
    }


    private void markTask(final Task task) {
        task.setSynced(false);
        DatabaseManager.getInstance(getApplicationContext()).updateTask(task);
        ConnectionManager.getInstance().markTask(task.getId(), new AsyncVolleyRequest<Task>() {
            @Override
            public void onResponse(Task response) {
                task.setSynced(true);
                task.setSyncedAt(UtilsDateTime.getISO8601String());
                DatabaseManager.getInstance(getApplicationContext()).updateTask(task);
                adapterNotifySetChanged();
            }

            @Override
            public void onError(VolleyError error) {

            }
        });
    }

    private void addTask(String title, String body) {
        final Task task = DatabaseManager.getInstance(getApplicationContext()).insertTask(title, body, currentUser.getId());
        adapterAddTask(task);
      

        ConnectionManager.getInstance().addTask(title, body, new AsyncVolleyRequest<Task>() {
            @Override
            public void onResponse(Task response) {
                Log.d(TAG, response.toString());
                task.setId(response.getId());
                task.setSynced(true);
                task.setSyncedAt(UtilsDateTime.getISO8601String());
                DatabaseManager.getInstance(getApplicationContext()).updateTask(task);
                adapterNotifySetChanged();
            }

            @Override
            public void onError(VolleyError error) {

            }
        });
    }

    private void addTaskRemote(final Task task) {
        ConnectionManager.getInstance().addTask(task.getTitle(), task.getBody(), new AsyncVolleyRequest<Task>() {
            @Override
            public void onResponse(Task response) {
                Log.d(TAG, response.toString());
                task.setId(response.getId());
                task.setSynced(true);
                task.setSyncedAt(UtilsDateTime.getISO8601String());
                DatabaseManager.getInstance(getApplicationContext()).updateTask(task);
                adapterNotifySetChanged();
            }

            @Override
            public void onError(VolleyError error) {

            }
        });
    }

    private Task findTaskById(int remoteId, List<Task> tasks) {
        for(Task task : tasks) {
            if(task.getId() == remoteId)
                return task;
        }
        return null;
    }

    private void getTasks() {
        ConnectionManager.getInstance().getTasks(new AsyncVolleyRequest<List<Task>>() {
            @Override
            public void onResponse(List<Task> responseTasks) {
                ArrayList<Task> dbTasks = DatabaseManager.getInstance(getApplicationContext()).getAllTasksIncludeDeleted(currentUser.getId());
                Task foundTask;
                for(Task task : responseTasks) {
                    foundTask = findTaskById(task.getId(), dbTasks);
                    dbTasks.remove(foundTask);
                    if(foundTask != null) {
                        if(foundTask.isDeleted()) { //task is deleted locally
                            Log.d(TAG, "Deleting task: " + foundTask.toString());
                            deleteTaskRemote(task);
                        }
                        else if(!foundTask.isSynced()){ //task is on server but not synced
                            Log.d(TAG, "Updating task: " + foundTask.toString());
                            //TODO prefer local or remote?
                            updateTaskRemote(foundTask);
                        }
                    }
                    else { //new task on server, but not locally
                        Log.d(TAG, "New task: " + task.toString());
                        task.setSynced(true);
                        final Task newTask = DatabaseManager.getInstance(getApplicationContext()).insertTask(task, currentUser.getId());
                        adapterAddTask(newTask);
                    }
                }

                for(Task task : dbTasks) {
                    if(!task.isDeleted())
                        addTaskRemote(task);
                }


                MainActivity.this.swipeRefreshLayout.setRefreshing(false);
                //MainActivity.this.showToast("Refreshed");
            }

            @Override
            public void onError(VolleyError error) {
                error.printStackTrace();
                MainActivity.this.swipeRefreshLayout.setRefreshing(false);
                MainActivity.this.showToast("Error occurred");
            }
        });
    }


    private void editTask(Task task) {
        Intent intent = new Intent();
        intent.setClass(this, ViewTaskActivity.class);
        intent.putExtra("TASK", task);
        startActivityForResult(intent, EDIT_TASK_REQUEST_CODE);
    }



    private void getTasksDB() {
        ArrayList<Task> dbTasks = DatabaseManager.getInstance(getApplicationContext()).getAllTasks(currentUser.getId());
        adapter.clear();
        adapter.addAll(dbTasks);
        for(Task task : dbTasks) {
            if(!task.isSynced()) {
                showToast("There are some unsynced tasks");
                break;
            }
        }
    }


    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }


}
