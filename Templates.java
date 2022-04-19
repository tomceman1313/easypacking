package com.easierlifeapps.easypacking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.easierlifeapps.easypacking.Adapters.AdapterTemplates;
import com.easierlifeapps.easypacking.Adapters.DatabaseOpenHelper;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class Templates extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;
    Button btnOffline, btnOnline;
    boolean online;
    /**
    * Aktivita sloužící k zobrazení přehledu šablon
    * Načítá data z lokální databáze i cloudové
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_templates);

        findViewById(R.id.btnEditToolbar).setVisibility(View.GONE);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        ImageView addList = findViewById(R.id.buttonAddTemplate);
        addList.setOnClickListener(v -> {
            Intent lists = new Intent(Templates.this, TemplateInfo.class);
            startActivity(lists);
            finish();
        });

        btnOffline = findViewById(R.id.btnOffline);
        btnOnline = findViewById(R.id.btnOnline);

        btnOnline.setOnClickListener(v -> {
            online = true;
            setButtonActive();
            if(checkUser()){
                LoadDataOnline();
            }
        });

        btnOffline.setOnClickListener(v -> {
            if(!isNetworkConnected()){
                Toast.makeText(Templates.this, "Device is not connected to internet!", Toast.LENGTH_SHORT).show();
                return;
            }
            online = false;
            setButtonActive();
            LoadData();
        });
        online = false;
        LoadData();
    }
    private void FillRecycler(final ArrayList<String> array){
        RecyclerView recyclerView = findViewById(R.id.listTemplates);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        AdapterTemplates adapterTemplates = new AdapterTemplates(array, online);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapterTemplates);
        recyclerView.getLayoutManager().scrollToPosition(0);

        adapterTemplates.setOnItemClickListener(new AdapterTemplates.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent;
                if(online){
                    intent = new Intent(Templates.this, FillListOnline.class);
                }else {
                    intent = new Intent(Templates.this, FillList.class);
                }
                if(online){
                    intent.putExtra("tableName", array.get(position));
                }else {
                    intent.putExtra("tableName", replaceSpace(array.get(position), true));
                }
                intent.putExtra("template", true);
                startActivity(intent);
                finish();
            }
        });
    }
    //Načítání dat z lokální databáze a následné zavolání funkce FillRecycler s načtenými daty
    private void LoadData(){
        DatabaseOpenHelper openHelper = new DatabaseOpenHelper(this);
        SQLiteDatabase database = openHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM table_templates ORDER BY table_name" , null);

        ArrayList<String> listsArray = new ArrayList<>();
        while(cursor.moveToNext()){
            listsArray.add(cursor.getString(1));
        }

        FillRecycler(listsArray);
        database.close();
    }
    // Načítání dat z cloudové databáze
    private void LoadDataOnline() {
        ArrayList<String> list = new ArrayList<>();

        DocumentReference documentReference = db.collection("templates").document(user.getEmail());

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    for (String table : (ArrayList<String>) documentSnapshot.get("tables")) {
                        list.add(table);
                    }
                    if(list.size() == 0){
                        Toast.makeText(Templates.this, "Create your first online packing list.", Toast.LENGTH_SHORT).show();
                    }
                    FillRecycler(list);
                } else {
                    Toast.makeText(Templates.this, "Create your first online packing list.", Toast.LENGTH_SHORT).show();
                    FillRecycler(new ArrayList<>());
                }
            }
        });


    }
    //Kontrola přihášení uživatele
    private boolean checkUser(){
        user = mAuth.getCurrentUser();
        if(user == null){
            startActivityForResult(new Intent(Templates.this, Login.class), 1);
            return false;
        }
        return true;


    }

    private void setButtonActive() {
        if (!online) {
            btnOffline.setBackgroundResource(R.color.colorPrimaryDark);
            btnOnline.setBackgroundResource(R.color.white);
            btnOnline.setTextColor(Color.parseColor("#525252"));
            btnOffline.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            btnOnline.setBackgroundResource(R.color.colorPrimaryDark);
            btnOffline.setBackgroundResource(R.color.white);
            btnOffline.setTextColor(Color.parseColor("#525252"));
            btnOnline.setTextColor(Color.parseColor("#FFFFFF"));
        }
    }
    // Nahrazení _ za mezeru nebo naopak
    private String replaceSpace(String name, boolean replaceSpace) {
        if (!replaceSpace) {
            return name.replace("_", " ");
        }
        return name.replace(" ", "_");
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Override
    public void onBackPressed() {
        Intent main = new Intent(Templates.this, MainActivity.class);
        startActivity(main);
        finish();
    }
}
