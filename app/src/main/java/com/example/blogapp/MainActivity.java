package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private FloatingActionButton newPostBtn;
    private String currentUSerId;
    private BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        newPostBtn = findViewById(R.id.newPostBtn);
        bottomNavigationView = findViewById(R.id.bottomNavBar);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        if (mAuth.getCurrentUser() != null) {

            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new HomeFragment()).commit();
            newPostBtn.setOnClickListener(onNewPostBtnClick);
            bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        }
    }


    FloatingActionButton.OnClickListener onNewPostBtnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(MainActivity.this, PostNewBlog.class));
            finish();
        }
    };

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    switch (item.getItemId()) {
                        case R.id.action_home:
                            selectedFragment = new HomeFragment();
                            Toast.makeText(MainActivity.this, "Home", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.action_notify:
                            selectedFragment = new MyNotificationFragment();
                            Toast.makeText(MainActivity.this, "Notification", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.action_account:

                            selectedFragment = new AccountFragment();
                            Toast.makeText(MainActivity.this, "Account", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    assert selectedFragment != null;
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, selectedFragment).commit();
                    return true;
                }
            };

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            sendToLogin();
        } else {
            currentUSerId = user.getUid();
            firestore.collection("Users").document(currentUSerId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (!task.getResult().exists())
                            sendToSetup();
                    }
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("Errorrrrrrrrrrr", "onDestroy" );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                logout();
               return true;
            case R.id.action_settings:
                sendToSetup();
                return true;
            default:
                return false;
        }

    }

    private void sendToSetup() {
        startActivity(new Intent(MainActivity.this, SetupActivity.class));
        finish();
    }

    private void logout() {
        mAuth.signOut();
        sendToLogin();
    }

    private void sendToLogin() {
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }


}
