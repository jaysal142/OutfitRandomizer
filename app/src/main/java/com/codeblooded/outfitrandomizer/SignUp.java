package com.codeblooded.outfitrandomizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.codeblooded.outfitrandomizer.util.UserHelperClass;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {

    TextInputLayout regUsername, regPhoneNo, regEmail, regPassword, regPasswordConf;
    Button regBtn, regToLoginBtn;

    FirebaseDatabase rootNode;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        regUsername = findViewById(R.id.username_signup);
        regPhoneNo = findViewById(R.id.phone_num_signup);
        regEmail = findViewById(R.id.email_signup);
        regPassword = findViewById(R.id.password_signup);
        regPasswordConf = findViewById(R.id.password_conf_signup);
        regBtn = findViewById(R.id.btn_create_acc_signup);
        regToLoginBtn = findViewById(R.id.btn_login_signup);

        //Save data to Firebase on button click
        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference("users");
    }

    public void backToLogin(View view) {
        Intent intent = new Intent(SignUp.this, Login.class);
        startActivity(intent);
    }

    private Boolean validateUsername() {
        //Username Checks
        String value = regUsername.getEditText().getText().toString();
        if (value.isEmpty()) {
            regUsername.setError("Please Enter A Valid Username");
            return false;
        } else if (value.matches(".*\\s+.*")) {
            regUsername.setError("Please Remove Spaces");
            return false;
        } else if (value.length() >= 15) {
            regUsername.setError("Username Too Long");
            return false;
        }
        else {
            regUsername.setError(null);
            regUsername.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePhoneNo() {
        //Phone Number Checks
        String value = regPhoneNo.getEditText().getText().toString();
        if (value.isEmpty()) {
            regPhoneNo.setError("Please Enter A Valid Phone Number");
            return false;
        }
        else {
            regPhoneNo.setError(null);
            regPhoneNo.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateEmail() {
        //Email Checks
        String value = regEmail.getEditText().getText().toString();
        if (value.isEmpty() || !value.matches("[a-zA-z0-9._-]+@[a-z]+\\.+[a-z]+")) {
            regEmail.setError("Please Enter A Valid Email");
            return false;
        }
        else {
            regEmail.setError(null);
            regEmail.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePassword() {
        //Password Checks
        String value = regPassword.getEditText().getText().toString();
        String passConf = regPasswordConf.getEditText().getText().toString();
        if (value.isEmpty()) {
            regPassword.setError("Please Enter A Valid Password");
            return false;
        } else if (!value.matches("^(?=\\S+$).{6,}$")) {
            regPassword.setError("Must Be At Least 6 Characters And Contain No Spaces");
            return false;
        } else if (passConf.isEmpty() || !passConf.equals(value)) {
            regPasswordConf.setError("Passwords Do Not Match");
            regPassword.setError(null);
            regPassword.setErrorEnabled(false);
            return false;
        }
        else {
            regPassword.setError(null);
            regPassword.setErrorEnabled(false);
            regPasswordConf.setError(null);
            regPasswordConf.setErrorEnabled(false);
            return true;
        }
    }

    public void registerUser(View view) {
        if(!validateUsername() || !validatePhoneNo() || !validateEmail() || !validatePassword()) {
            return;
        }

        //Get text field values
        String username = regUsername.getEditText().getText().toString();
        String phoneNo = regPhoneNo.getEditText().getText().toString();
        String email = regEmail.getEditText().getText().toString();
        String password = regPassword.getEditText().getText().toString();

        UserHelperClass helperClass = new UserHelperClass(username, phoneNo, email, password);

        reference.child(username).setValue(helperClass);

        backToLogin(view);
    }
}