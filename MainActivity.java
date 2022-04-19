package com.easierlifeapps.easypacking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivity extends AppCompatActivity {
    /**
     * Úvodní aktivita sloužící jako rozcestník do dalších sekcí aplikace*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.btnEditToolbar).setVisibility(View.GONE);
        findViewById(R.id.btnBack).setVisibility(View.GONE);

        ConstraintLayout buttonLists = findViewById(R.id.buttonLists);
        buttonLists.setOnClickListener(v -> {
            Intent lists = new Intent(MainActivity.this, Lists.class);
            startActivity(lists);
            finish();
        });

        ConstraintLayout btnTemplates = findViewById(R.id.btnTemplates);
        btnTemplates.setOnClickListener(v -> {
            Intent lists = new Intent(MainActivity.this, Templates.class);
            startActivity(lists);
            finish();
        });
        ConstraintLayout btnJournal = findViewById(R.id.buttonJournal);
        btnJournal.setOnClickListener(v -> {
            Intent profile = new Intent(MainActivity.this, Journal.class);
            startActivity(profile);
            finish();
        });

        ConstraintLayout btnProfile = findViewById(R.id.buttonProfile);
        btnProfile.setOnClickListener(v -> {
            Intent profile = new Intent(MainActivity.this, Profile.class);
            startActivity(profile);
            finish();
        });
    }


}
