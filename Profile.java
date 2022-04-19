package com.easierlifeapps.easypacking;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Profile extends AppCompatActivity {
    FirebaseUser user;
    FirebaseAuth auth;
    Button btnLogOut;
    /**
    * Aktivita složící k zobrazení přihlášeného uživatele
     * Uživatel má možnost se zde odhlásit*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        findViewById(R.id.btnEditToolbar).setVisibility(View.GONE);
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        auth = FirebaseAuth.getInstance();

        btnLogOut = findViewById(R.id.btnLogOut);
        fillUserEmail();
        btnLogOut.setOnClickListener(v -> {
            if(checkUser()){
                auth.signOut();
                fillUserEmail();
                Toast.makeText(Profile.this, "Logged out", Toast.LENGTH_SHORT).show();
            }else{
                startActivityForResult(new Intent(Profile.this, Login.class), 1);
            }
        });

    }

    private void fillUserEmail(){
        EditText userEmail = findViewById(R.id.inputEmail);

        if(checkUser()) {
            userEmail.setText(auth.getCurrentUser().getEmail());
        }else {
            userEmail.setText("...");
            btnLogOut.setText("Log in");
        }
    }

    private boolean checkUser(){
        user = auth.getCurrentUser();
        if(user == null){
            //startActivityForResult(new Intent(Profile.this, Login.class), 1);
            return false;
        }
        return true;


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                Toast.makeText(this, "Login successful.", Toast.LENGTH_SHORT).show();
                fillUserEmail();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent main = new Intent(Profile.this, MainActivity.class);
        startActivity(main);
        System.gc();
        finish();
    }
}
