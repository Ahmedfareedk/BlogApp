package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterationActivity extends AppCompatActivity {
    private  EditText emailEditText, passEditText, confPassEditText;
    private Button signUpBtn;
    private TextView alreadyHaveAcc;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration);
        emailEditText = findViewById(R.id.signupEmailEditText);
        passEditText = findViewById(R.id.signupPasswordEditText);
        confPassEditText = findViewById(R.id.conPasswordEditText);
        signUpBtn = findViewById(R.id.createAccBtn);
        alreadyHaveAcc = findViewById(R.id.alreayHaveAccTV);
        progressBar = findViewById(R.id.signupPB);
        mAuth =FirebaseAuth.getInstance();



        signUpBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String userEmail = emailEditText.getText().toString();
                String password = passEditText.getText().toString();
                String confPassword = confPassEditText.getText().toString();
                if(!TextUtils.isEmpty(userEmail) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confPassword))
                {

                    if(password.equals(confPassword))
                    {
                        progressBar.setVisibility(View.VISIBLE);
                        mAuth.createUserWithEmailAndPassword(userEmail, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful())
                                {
                                    Toast.makeText(RegisterationActivity.this, "Account created Successfully!", Toast.LENGTH_SHORT).show();
                                    sendToSettings();
                                }else{
                                    Toast.makeText(RegisterationActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });

                    }else{

                        Toast.makeText(RegisterationActivity.this, "password doesn't match!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(RegisterationActivity.this, "complete missed fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        alreadyHaveAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToLogin();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null)
        {
            sendToMain();
        }
    }

    private void sendToLogin() {
        startActivity(new Intent(RegisterationActivity.this, LoginActivity.class));
        finish();
    }
    private void sendToMain() {
        startActivity(new Intent(RegisterationActivity.this, MainActivity.class));
        finish();
    }
    private void sendToSettings()
    {
        startActivity(new Intent(RegisterationActivity.this, SetupActivity.class));
        finish();
    }

}
