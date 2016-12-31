package com.uz.simpletodolist;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.uz.simpletodolist.core.AsyncVolleyRequest;
import com.uz.simpletodolist.core.ConnectionManager;
import com.uz.simpletodolist.core.DatabaseManager;
import com.uz.simpletodolist.model.User;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    public static final String TAG = "LoginActivity";

    public static final String KEY_EMAIL = "EMAIL";
    public static final String KEY_PASSWORD = "PASSWORD";
    public static final String KEY_AUTOLOGIN = "AUTOLOGIN";

    public static final int REGISTER_REQUEST_CODE = 2;

    SharedPreferences prefs;

    Button btnLogin;
    Button btnRegister;
    EditText txtEmail;
    EditText txtPassword;
    CheckBox boxAutoLogin;
    ProgressBar loginProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Login");
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        boxAutoLogin = (CheckBox) findViewById(R.id.boxAutoLogin);
        loginProgress = (ProgressBar) findViewById(R.id.loginProgress);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(intent, REGISTER_REQUEST_CODE);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = txtEmail.getText().toString().trim();
                String password = txtPassword.getText().toString().trim();
                if(email.isEmpty() || password.isEmpty()) {
                    if(email.isEmpty())
                        txtEmail.setError("Must enter email");
                    if(password.isEmpty())
                        txtPassword.setError("Must enter password");
                    return;
                }

                login(email, password);

            }
        });

        boxAutoLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(KEY_AUTOLOGIN, isChecked);
                editor.apply();
            }
        });

        boxAutoLogin.setChecked(prefs.getBoolean(KEY_AUTOLOGIN, false));
        if(savedInstanceState == null) {
            getLogin();
            if(prefs.getBoolean(KEY_AUTOLOGIN, false)
                    && !getIntent().getBooleanExtra("LOGGEDOUT", false)) {
                btnLogin.performClick();
            }
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_CANCELED) return;
        if(requestCode == REGISTER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if(data == null) return;
            String email = data.getStringExtra("EMAIL");
            String password = data.getStringExtra("PASSWORD");
            txtEmail.setText(email);
            txtPassword.setText(password);
            btnLogin.performClick();
        }
    }

    private void saveLogin() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_EMAIL, txtEmail.getText().toString().trim());
        editor.putString(KEY_PASSWORD, txtPassword.getText().toString().trim());
        editor.apply();
    }

    private void getLogin() {
        txtEmail.setText(prefs.getString(KEY_EMAIL, ""));
        txtPassword.setText(prefs.getString(KEY_PASSWORD, ""));

    }

    private void setLoggingIn(boolean loggingIn) {
        if(loggingIn) {
            txtEmail.setEnabled(false);
            txtPassword.setEnabled(false);
            btnLogin.setVisibility(View.INVISIBLE);
            btnRegister.setVisibility(View.INVISIBLE);
            boxAutoLogin.setVisibility(View.INVISIBLE);
            loginProgress.setVisibility(View.VISIBLE);
        }
        else {
            txtEmail.setEnabled(true);
            txtPassword.setEnabled(true);
            btnLogin.setVisibility(View.VISIBLE);
            btnRegister.setVisibility(View.VISIBLE);
            boxAutoLogin.setVisibility(View.VISIBLE);
            loginProgress.setVisibility(View.INVISIBLE);
        }
    }

    private void loggedIn(String email, String password) {
        saveLogin();
        User user = DatabaseManager.getInstance(getApplicationContext()).getUser(email, password);
        if(user == null)
            user = DatabaseManager.getInstance(getApplicationContext()).insertUser(email, password);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("USER", user);
        finish();
        startActivity(intent);
    }
//"test123@gmail.com", "testtest"
    private void login(final String email, final String password) {
        setLoggingIn(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                ConnectionManager.getInstance(getApplicationContext()).login(email, password, new AsyncVolleyRequest<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(LoginActivity.this, "Logged in!", Toast.LENGTH_SHORT).show();
                        loggedIn(email, password);
                        setLoggingIn(false);
                    }

                    @Override
                    public void onError(VolleyError error) {
                        setLoggingIn(false);
                        if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                            Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(LoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();

    }
}
