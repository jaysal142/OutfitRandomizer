package com.codeblooded.outfitrandomizer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootNode = FirebaseDatabase.getInstance();
                reference = rootNode.getReference("users");

                if (regUsername.getEditText().getText().toString().isEmpty()) {
                    Toast.makeText(SignUp.this, "Please Enter A Valid Username", Toast.LENGTH_SHORT).show();
                    return;
                } else if (regPhoneNo.getEditText().getText().toString().isEmpty()) {
                    Toast.makeText(SignUp.this, "Please Enter A Valid Phone Number", Toast.LENGTH_SHORT).show();
                    return;
                } else if (regEmail.getEditText().getText().toString().isEmpty()) {
                    Toast.makeText(SignUp.this, "Please Enter A Valid Email", Toast.LENGTH_SHORT).show();
                    return;
                } else if (regPassword.getEditText().getText().toString().isEmpty()) {
                    Toast.makeText(SignUp.this, "Please Enter A Valid Password", Toast.LENGTH_SHORT).show();
                    return;
                } else if (regPasswordConf.getEditText().getText().toString().isEmpty() || !regPasswordConf.getEditText().getText().toString().equals(regPassword.getEditText().getText().toString())) {
                    Toast.makeText(SignUp.this, "Passwords Do Not Match", Toast.LENGTH_LONG).show();
                    return;
                }

                //Get text field values
                String username = regUsername.getEditText().getText().toString();
                String phoneNo = regPhoneNo.getEditText().getText().toString();
                String email = regEmail.getEditText().getText().toString();
                String password = regPassword.getEditText().getText().toString();

                UserHelperClass helperClass = new UserHelperClass(username, phoneNo, email, password);

                reference.child(phoneNo).setValue(helperClass);

            }
        });
    }
}