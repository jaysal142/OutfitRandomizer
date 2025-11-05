package com.codeblooded.outfitrandomizer;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.codeblooded.outfitrandomizer.data.local.AppDatabase;
import com.codeblooded.outfitrandomizer.data.local.UserDao;
import com.codeblooded.outfitrandomizer.data.local.UserEntity;
import com.codeblooded.outfitrandomizer.util.AppExecutors;

public class Login extends AppCompatActivity {

    private UserDao userDao;

    Button callSignUp, login_btn;
    ImageView image;
    TextView header;
    TextInputLayout username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userDao = AppDatabase.getInstance(getApplicationContext()).userDao();

        image = findViewById(R.id.brand_logo_login);
        header = findViewById(R.id.header_login);
        username = findViewById(R.id.username_login);
        password = findViewById(R.id.password_login);
    }

    private Boolean validateUsername() {
        String value = username.getEditText().getText().toString();

        if (value.isEmpty()) {
            username.setError("Field Cannot Be Empty");
            return false;
        }
        else {
            username.setError(null);
            username.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePassword() {
        String value = password.getEditText().getText().toString();

        if (value.isEmpty()) {
            password.setError("Field Cannot Be Empty");
            return false;
        }
        else {
            password.setError(null);
            password.setErrorEnabled(false);
            return true;
        }
    }

    private void isUser() {
        String reqUsername = username.getEditText().getText().toString().trim();
        String reqPassword = password.getEditText().getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUser = reference.orderByChild("username").equalTo(reqUsername);

        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    username.setError("Username Not Found");
                    username.requestFocus();
                    return;
                }

                DataSnapshot node = dataSnapshot.child(reqUsername);

                if (node.getValue() == null) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        node = child;
                        break;
                    }
                }

                String passwordFromDB = node.child("password").getValue(String.class);
                if (passwordFromDB == null || !passwordFromDB.equals(reqPassword)) {
                    password.setError("Incorrect Password");
                    password.requestFocus();
                    return;
                }

                String usernameFromDB = node.child("username").getValue(String.class);
                String phoneNoFromDB = node.child("phoneNo").getValue(String.class);
                String emailFromDB = node.child("email").getValue(String.class);

                UserEntity localUser = new UserEntity(usernameFromDB, phoneNoFromDB, emailFromDB);
                AppExecutors.io().execute(() -> userDao.upsert(localUser));

                getSharedPreferences("session", MODE_PRIVATE).edit().putString("current_username", usernameFromDB).apply();

                Intent intent = new Intent(getApplicationContext(), HomePage.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void loginUser(View view) {
        //Validate login info
        if (!validateUsername() || !validatePassword()) {
            return;
        }
        else {
            isUser();
        }
    }

    public void callSignUpScreen(View view) {
        Intent intent = new Intent(Login.this, SignUp.class);

        Pair[] pairs = new Pair[2];
        pairs[0] = new Pair<View,String>(image, "logo_transition");
        pairs[1] = new Pair<View,String>(header, "header_transition");

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Login.this,pairs);
        startActivity(intent, options.toBundle());
    }
}