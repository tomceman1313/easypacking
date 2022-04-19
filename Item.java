package com.easierlifeapps.easypacking;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.easierlifeapps.easypacking.Adapters.DatabaseOpenHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Item extends AppCompatActivity {
    DatabaseOpenHelper openHelper;
    SQLiteDatabase database;
    Bundle extras;

    EditText inputName;
    EditText inputDays;
    EditText inputNotes;
    Spinner spinnerCategory;
    Spinner spinnerColor;
    ArrayAdapter<String> arrayAdapter, arrayAdapterColor;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser user;
    Map<String, String > colors;
    String collectionName;

    /**
     *Aktivita složící pro práci s položkou
     * Položku lze buď vytvářet nebo editovat
     * Slouží pro práci s položkami seznamů i šablon (online i offline)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        findViewById(R.id.btnDelete).setVisibility(View.GONE);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        colors = new HashMap<>();
        colors.put("White", "#FFFFFF");
        colors.put("Green", "#008000");
        colors.put("Yellow", "#FFFF00");
        colors.put("Red", "#FF0000");
        colors.put("Blue", "#0000FF");
        colors.put("Purple", "#800080");
        colors.put("Orange", "#EB8739");

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        openHelper = new DatabaseOpenHelper(this);
        database = openHelper.getWritableDatabase();
        extras = getIntent().getExtras();
        if(extras.getBoolean("template")){
            collectionName = "templates";
        }else {
            collectionName = "lists";
        }

        List<String> arrayType = new ArrayList<>();
        arrayType.add("Clothes");
        arrayType.add("Hygiene");
        arrayType.add("Health");
        arrayType.add("Documents");
        arrayType.add("Devices");
        arrayType.add("Other");
        arrayType.add("Food");
        arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arrayType);

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory = findViewById(R.id.spinnerType);
        spinnerCategory.setAdapter(arrayAdapter);
        spinnerCategory.setSelection(arrayAdapter.getPosition(extras.getString("category")));



        List<String> arrayColor = new ArrayList<>();
        arrayColor.add("White");
        arrayColor.add("Green");
        arrayColor.add("Yellow");
        arrayColor.add("Red");
        arrayColor.add("Blue");
        arrayColor.add("Purple");
        arrayColor.add("Orange");
        arrayAdapterColor = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arrayColor);

        arrayAdapterColor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerColor = findViewById(R.id.spinnerColor);
        spinnerColor.setAdapter(arrayAdapterColor);

        inputName = findViewById(R.id.inputName);
        inputDays = findViewById(R.id.txtDays);
        inputNotes = findViewById(R.id.inputNotes);

        ImageButton btnPiecePlus = findViewById(R.id.btnPiecePlus);
        ImageButton btnPieceMinus = findViewById(R.id.btnPieceMinus);

        btnPieceMinus.setOnClickListener(v -> {
            int days = 0;
            if(!inputDays.getText().toString().equals("Number of days")){
                days = Integer.parseInt(inputDays.getText().toString());
            }
            if((days - 1) <= 0){
                inputDays.setText("Number of days");
            }else{
                inputDays.setText(String.valueOf(days - 1));
            }
        });

        btnPiecePlus.setOnClickListener(v -> {
            if(inputDays.getText().toString().equals("Number of days")){
                inputDays.setText("1");
            }else{
                int days = Integer.parseInt(inputDays.getText().toString());
                inputDays.setText(String.valueOf(days + 1));
            }
        });

        ImageView btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(v -> {
            if(extras.getBoolean("online")){
                if(!extras.getString("path").equals("")){
                    db.collection(collectionName).document(extras.getString("path")).collection(extras.getString("tableName")).document(extras.getString("name")).delete();
                }else {
                    db.collection(collectionName).document(user.getEmail()).collection(extras.getString("tableName")).document(extras.getString("name")).delete();
                }
            }else {
                database.delete(extras.getString("tableName"), "name = ?", new String[]{extras.getString("name")});
                System.out.println("Item deleted");
                database.close();
            }
            Intent intent = new Intent();
            intent.putExtra("refresh", 1);
            setResult(RESULT_OK, intent);
            database.close();
            finish();
        });

        Button btnSave = findViewById(R.id.btnSaveChanges);
        btnSave.setOnClickListener(v -> {
            String name = inputName.getText().toString();
            int days;
            if(inputDays.getText().toString().equals("Number of days")){
                days = 0;
            }else {
                days = Integer.parseInt(inputDays.getText().toString());
            }
            String notes = inputNotes.getText().toString();
            String type = spinnerCategory.getSelectedItem().toString();
            String color = spinnerColor.getSelectedItem().toString();
            if (TextUtils.isEmpty(name)) {
                inputName.setError("Name cannot be empty");
                inputName.requestFocus();
                return;
            }

            ContentValues valuesItem = new ContentValues();
            valuesItem.put("name", name);
            valuesItem.put("pcs", days);
            valuesItem.put("category", type);
            valuesItem.put("notes", notes);
            valuesItem.put("color", colors.get(color));

            if(extras.getBoolean("fill")){
                if(!uniqueCheck(name)){
                    return;
                }
                if(extras.getBoolean("online")){
                    Map<String, Object> updatedItem = new HashMap<>();
                    updatedItem.put("pcs", days);
                    updatedItem.put("category", type);
                    updatedItem.put("notes", notes);
                    updatedItem.put("packed", 0);
                    updatedItem.put("color", colors.get(color));

                    if(!extras.getString("path").equals("")){
                        db.collection(collectionName).document(extras.getString("path")).collection(extras.getString("tableName")).document(extras.getString("name")).delete();
                        db.collection(collectionName).document(extras.getString("path")).collection(extras.getString("tableName")).document(name).set(updatedItem);
                    }else {
                        db.collection(collectionName).document(user.getEmail()).collection(extras.getString("tableName")).document(extras.getString("name")).delete();
                        db.collection(collectionName).document(user.getEmail()).collection(extras.getString("tableName")).document(name).set(updatedItem);
                    }
                }else {
                    database.update(extras.getString("tableName"), valuesItem, "name = ?", new String[]{extras.getString("name")});
                    System.out.println("Updated");
                }
            }else {
                if(!uniqueCheck(name)){
                    return;
                }
                if(extras.getBoolean("online")){
                    Map<String, Object> updatedItem = new HashMap<>();
                    updatedItem.put("pcs", days);
                    updatedItem.put("category", type);
                    updatedItem.put("notes", notes);
                    updatedItem.put("packed", 0);
                    updatedItem.put("color", colors.get(color));

                    if(!extras.getString("path").equals("")){
                        db.collection(collectionName).document(extras.getString("path")).collection(extras.getString("tableName")).document(name).set(updatedItem);
                    }else {
                        db.collection(collectionName).document(user.getEmail()).collection(extras.getString("tableName")).document(name).set(updatedItem);
                    }
                }else {
                    valuesItem.put("packed", 0);
                    database.insert(extras.getString("tableName"), null, valuesItem);
                }
            }
            Intent intent = new Intent();
            intent.putExtra("refresh", 1);
            setResult(RESULT_OK, intent);
            database.close();
            finish();
        });

        if(extras.getBoolean("fill")){
            fillItemInfo();
            findViewById(R.id.btnDelete).setVisibility(View.VISIBLE);
            TextView txtItemName = findViewById(R.id.itemName);
            txtItemName.setText("Item");
        }
    }

    private void fillItemInfo(){
        inputName.setText(extras.getString("name"));
        inputDays.setText((extras.getInt("pcs") == 0)? "Number of days": String.valueOf(extras.getInt("pcs")));
        inputNotes.setText(extras.getString("notes"));
        spinnerCategory.setSelection(arrayAdapter.getPosition(extras.getString("category")));
        spinnerColor.setSelection(arrayAdapterColor.getPosition(findColorName(extras.getString("color"))));

    }

    private String findColorName(String code){
        for (Map.Entry<String, String> entry : colors.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if(code.equals(value)){
                return key;
            }
        }
        return "White";
    }

    private boolean uniqueCheck(String name){
        String tableName = extras.getString("tableName");
        if(extras.getBoolean("fill")){
            if(name.equals(extras.getString("name"))){
                return true;
            }
        }
        if(extras.getBoolean("online")){
            return true;
        }
        DatabaseOpenHelper openHelper = new DatabaseOpenHelper(this);
        SQLiteDatabase database = openHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + tableName + " WHERE name = ?", new String[]{name});

        if(cursor.getCount() > 0){
            Toast.makeText(this, "Name of the item is already used", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("refresh", 1);
        setResult(RESULT_OK, intent);
        database.close();
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}
