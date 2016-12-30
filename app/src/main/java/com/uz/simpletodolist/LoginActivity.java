package com.uz.simpletodolist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {
    public static final String TAG = "LoginActivity";

    public static final int REGISTER_REQUEST_CODE = 2;
    public static final int REGISTER_DONE_REQUEST_CODE = 3;
    public static final int REGISTER_CANCELED_REQUEST_CODE = 3;


    Button btnLogin;
    Button btnRegister;
    EditText txtEmail;
    EditText txtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtPassword = (EditText) findViewById(R.id.txtPassword);

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
                //TODO login
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == REGISTER_CANCELED_REQUEST_CODE) return;
        if(resultCode == REGISTER_DONE_REQUEST_CODE) {
            if(data == null) return;
            String email = data.getStringExtra("EMAIL");
            String password = data.getStringExtra("PASSWORD");
            //TODO login
        }
    }
}
