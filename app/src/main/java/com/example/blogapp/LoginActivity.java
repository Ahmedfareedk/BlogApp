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

public class LoginActivity extends AppCompatActivity {

  private EditText emailEditText, passwordEditText;
  private TextView signupTV;
   private Button loginBtn;
   private ProgressBar loginPB;
   private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signupTV = findViewById(R.id.signUpTextView);
        loginBtn = findViewById(R.id.loginBtn);
        loginPB = findViewById(R.id.loginPB);


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userEmail = emailEditText.getText().toString();
                String userPass = passwordEditText.getText().toString();


                if (!TextUtils.isEmpty(userEmail) && !TextUtils.isEmpty(userPass)) {

                    loginPB.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(userEmail, userPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful())
                            {
                                sendToMain();
                            }else{
                                String errorMsg = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                            }
                            loginPB.setVisibility(View.INVISIBLE);
                        }
                    });
                }else{
                    Toast.makeText(LoginActivity.this, "Email or Password field can't be empty!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        signupTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToRegistration();
            }
        });


    }

    private void sendToRegistration() {
        startActivity(new Intent(LoginActivity.this, RegisterationActivity.class));
        finish();
    }


    private void sendToMain() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null)
        {
           sendToMain();
        }
    }

    public void sendVerficationMal()
    {
        FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(LoginActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(LoginActivity.this, "Wrong!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
