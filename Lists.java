package com.easierlifeapps.easypacking;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.easierlifeapps.easypacking.Adapters.AdapterLists;
import com.easierlifeapps.easypacking.Adapters.DatabaseOpenHelper;
import com.easierlifeapps.easypacking.Adapters.ListsAplikator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class Lists extends AppCompatActivity {
    DatabaseOpenHelper openHelper;
    SQLiteDatabase database;
    Button btnSingle, btnGroup;
    int group;
    FirebaseFirestore db;
    int packedItemsOnline;
    int allItemsOnline;
    FirebaseAuth mAuth;
    FirebaseUser user;
    RecyclerView recyclerView;

    EditText inputOwner, inputListName;
    /**
     * Aktivita sloužící k zobrazení seznamů
     * Načítá zástupce lokálních i cloudových seznamů*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lists);

        openHelper = new DatabaseOpenHelper(this);
        database = openHelper.getReadableDatabase();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        inputOwner = findViewById(R.id.inputOwnerEmail);
        inputListName = findViewById(R.id.inputListName);

        findViewById(R.id.btnEditToolbar).setVisibility(View.GONE);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        ImageView addList = findViewById(R.id.buttonAddList);
        addList.setOnClickListener(v -> {
            Intent lists = new Intent(Lists.this, ListInfo.class);
            lists.putExtra("tableName", "new");
            startActivity(lists);
            finish();
        });

        btnSingle = findViewById(R.id.btnSingle);
        btnGroup = findViewById(R.id.btnGroup);

        btnGroup.setOnClickListener(v -> {
            if(!isNetworkConnected()){
                Toast.makeText(Lists.this, "Device is not connected to internet!", Toast.LENGTH_SHORT).show();
                return;
            }
            group = 1;
            setButtonActive();
            if(checkUser()){
                LoadDataOnline();
            }
        });

        btnSingle.setOnClickListener(v -> {
            group = 0;
            setButtonActive();
            LoadData();
        });
        group = 0;
        LoadData();
    }

    private void FillRecycler(final ArrayList<ListsAplikator> array) {
        recyclerView = findViewById(R.id.listLists);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        AdapterLists adapterLists = new AdapterLists(array);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapterLists);
        adapterLists.setOnItemClickListener(position -> {
            Intent intent;
            if(group == 1){
                intent = new Intent(Lists.this, FillListOnline.class);
                intent.putExtra("tableName", array.get(position).getName());
            }else {
                intent = new Intent(Lists.this, FillList.class);
                intent.putExtra("tableName", replaceSpace(array.get(position).getName(), true));
            }
            intent.putExtra("template", false);
            startActivity(intent);
            finish();
        });
    }

    private void LoadData() {
        Cursor cursor = database.rawQuery("SELECT * FROM table_lists ORDER BY id DESC", null);

        ArrayList<ListsAplikator> listsArray = new ArrayList<>();
        while (cursor.moveToNext()) {

            listsArray.add(new ListsAplikator(cursor.getString(1), countProgress(cursor.getString(1))));
        }

        FillRecycler(listsArray);
    }

    private void LoadDataOnline() {
        ArrayList<ListsAplikator> list = new ArrayList<>();

        DocumentReference documentReference = db.collection("lists").document(user.getEmail());
        System.out.println(user.getEmail());
        documentReference.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                ArrayList<String> array = (ArrayList<String>) documentSnapshot.get("tables");
                if(array.size() == 0){
                    Toast.makeText(Lists.this, "Create your first online packing list.", Toast.LENGTH_SHORT).show();
                    FillRecycler(new ArrayList<ListsAplikator>());
                }

                for (String table : (ArrayList<String>) documentSnapshot.get("tables")) {
                    CollectionReference docRef = db.collection("lists").document(user.getEmail()).collection(table);
                    docRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot documentSnapshot1 : queryDocumentSnapshots) {
                            if(!documentSnapshot1.getId().equals("table_info")){
                                if ((long) documentSnapshot1.get("packed") == 1) {
                                    ++packedItemsOnline;
                                }
                                ++allItemsOnline;
                            }
                        }
                        float result = ((float) packedItemsOnline / allItemsOnline) * 100;

                        list.add(new ListsAplikator(table, (int)result));
                        if(list.size() == 0){
                            Toast.makeText(Lists.this, "Create your first online packing list.", Toast.LENGTH_SHORT).show();
                        }
                        FillRecycler(list);
                    });

                }
            } else {
                Toast.makeText(Lists.this, "Create your first online packing list.", Toast.LENGTH_SHORT).show();
                FillRecycler(new ArrayList<ListsAplikator>());
            }
        });

    }

    private boolean checkUser(){
        user = mAuth.getCurrentUser();
        if(user == null){
            startActivityForResult(new Intent(Lists.this, Login.class), 1);
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
                //System.out.println("UPDATE");
                if(checkUser()) {
                    LoadDataOnline();
                }
            }
        }
    }
    /**
     * Funkce sloužící ke spočtení procentního zastoupení zabalených položek v seznamu
     * @param tableName jméno seznamu pro který je progres počítán*/
    private int countProgress(String tableName) {
        Cursor cursorTable = database.rawQuery("SELECT * FROM " + replaceSpace(tableName, true), null);
        int packedItems = 0;
        int allItems = 0;
        while (cursorTable.moveToNext()) {
            ++allItems;
            if (cursorTable.getInt(6) == 1) {
                ++packedItems;
            }
        }
        float result = ((float) packedItems / allItems) * 100;
        return (int) result;
    }

    private void setButtonActive() {
        if (group == 0) {
            btnSingle.setBackgroundResource(R.color.colorPrimaryDark);
            btnGroup.setBackgroundResource(R.color.white);
            btnGroup.setTextColor(Color.parseColor("#525252"));
            btnSingle.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            btnGroup.setBackgroundResource(R.color.colorPrimaryDark);
            btnSingle.setBackgroundResource(R.color.white);
            btnSingle.setTextColor(Color.parseColor("#525252"));
            btnGroup.setTextColor(Color.parseColor("#FFFFFF"));
        }
    }


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
        Intent main = new Intent(Lists.this, MainActivity.class);
        startActivity(main);
        finish();
    }
}
