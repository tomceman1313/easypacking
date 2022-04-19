package com.easierlifeapps.easypacking;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    EditText email, password;
    TextView register;
    Button btnLogin;
    boolean logUser;

    FirebaseAuth mAuth;
    /**
     * Aktivita sloužící k registraci a přihlášení uživatele*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViewById(R.id.btnEditToolbar).setVisibility(View.GONE);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        logUser = true;

        email = findViewById(R.id.inputEmail);
        password = findViewById(R.id.inputPassword);
        register = findViewById(R.id.btnNewAcc);
        btnLogin = findViewById(R.id.btnLogin);

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(view -> {
            if (logUser) {
                loginUser();
            } else {
                createUser();
            }
        });

        register.setOnClickListener(view -> {
            if(logUser){
                register.setText("Login here");
                btnLogin.setText("Register");
                logUser = false;
            }else {
                register.setText("Register here");
                btnLogin.setText("Login");
                logUser = true;
            }
        });
    }

    private void createUser() {
        String email = this.email.getText().toString();
        String password = this.password.getText().toString();

        if (TextUtils.isEmpty(email)) {
            this.email.setError("Email cannot be empty");
            this.email.requestFocus();
        } else if (TextUtils.isEmpty(password)) {
            this.password.setError("Password cannot be empty");
            this.password.requestFocus();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(Login.this, "User registration successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.putExtra("logged", 1);
                        setResult(RESULT_OK, intent);
                        finish();
                    }else {
                        Toast.makeText(Login.this, "Registration error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void loginUser() {
        String email = this.email.getText().toString();
        String password = this.password.getText().toString();

        if (TextUtils.isEmpty(email)) {
            this.email.setError("Email cannot be empty");
            this.email.requestFocus();
        } else if (TextUtils.isEmpty(password)) {
            this.password.setError("Password cannot be empty");
            this.password.requestFocus();
        } else {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(Login.this, "User logged in successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.putExtra("logged", 1);
                        setResult(RESULT_OK, intent);
                        finish();
                    }else {
                        Toast.makeText(Login.this, "Login error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Login.this, MainActivity.class);
        startActivity(intent);
    }
}
