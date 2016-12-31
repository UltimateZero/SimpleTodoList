package com.uz.simpletodolist;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.uz.simpletodolist.core.AsyncVolleyRequest;
import com.uz.simpletodolist.core.ConnectionManager;

import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {
    public static final String TAG = "RegisterActivity";

    Button btnSubmit;
    EditText txtEmail;
    EditText txtPassword;
    EditText txtConfirmPassword;
    ProgressBar registerProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        txtConfirmPassword = (EditText) findViewById(R.id.txtConfirmPassword);
        registerProgress = (ProgressBar) findViewById(R.id.registerProgress);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = txtEmail.getText().toString().trim();
                String password = txtPassword.getText().toString().trim();
                String confirmPassword = txtConfirmPassword.getText().toString().trim();
                if(email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    if(email.isEmpty())
                        txtEmail.setError("Must enter email");
                    if(password.isEmpty())
                        txtPassword.setError("Must enter password");
                    if(confirmPassword.isEmpty())
                        txtConfirmPassword.setError("Must repeat password here");
                    return;
                }
                if(!password.equals(confirmPassword)) {
                    txtConfirmPassword.setError("Passwords do not match");
                    return;
                }
                
                register(email, password);
                
            }
        });
    }

    private void setRegistering(boolean registering) {
        if(registering) {
            txtEmail.setEnabled(false);
            txtPassword.setEnabled(false);
            txtConfirmPassword.setEnabled(false);
            btnSubmit.setVisibility(View.INVISIBLE);
            registerProgress.setVisibility(View.VISIBLE);
        }
        else {
            txtEmail.setEnabled(true);
            txtPassword.setEnabled(true);
            txtConfirmPassword.setEnabled(true);
            btnSubmit.setVisibility(View.VISIBLE);
            registerProgress.setVisibility(View.INVISIBLE);
        }
    }

    private void register(final String email, final String password) {
        setRegistering(true);
        ConnectionManager.getInstance(getApplicationContext()).register(email, password, new AsyncVolleyRequest<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(RegisterActivity.this, "Successfully registered!", Toast.LENGTH_SHORT).show();
                registered(email, password);
                setRegistering(false);
            }

            @Override
            public void onError(VolleyError error) {
                setRegistering(false);
                Toast.makeText(RegisterActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registered(String email, String password) {
        Intent intent = new Intent();
        intent.putExtra("EMAIL", email);
        intent.putExtra("PASSWORD", password);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
