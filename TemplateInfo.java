package com.easierlifeapps.easypacking;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.easierlifeapps.easypacking.Adapters.DatabaseOpenHelper;
import com.easierlifeapps.easypacking.Adapters.SingleListAplikator;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateInfo extends AppCompatActivity {
    Bundle extras;
    DatabaseOpenHelper openHelper;
    SQLiteDatabase database;
    EditText templateName;
    boolean download;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser user;
    EditText txtOwner, txtTemplateName;
    /**
    * Aktivita sloužící k zobrazení detailu šablony
     * Slouží jak pro lokální šablonu tak pro online*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.template_info);
        findViewById(R.id.contDownload).setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user == null) {
            startActivityForResult(new Intent(TemplateInfo.this, Login.class), 1);
        }

        openHelper = new DatabaseOpenHelper(this);
        database = openHelper.getWritableDatabase();

        db = FirebaseFirestore.getInstance();

        extras = getIntent().getExtras();
        download = false;

        templateName = findViewById(R.id.inputName);
        if (extras != null) {
            templateName.setText(replaceSpace(extras.getString("tableName"), false));
            if (extras.getBoolean("online")) {
                templateName.setFocusable(false);
            }
            findViewById(R.id.textView12).setVisibility(View.GONE);
            findViewById(R.id.linearLayout2).setVisibility(View.GONE);
            TextView txt = findViewById(R.id.textView3);
            txt.setText("Name of template");
        } else {
            findViewById(R.id.btnDelete).setVisibility(View.GONE);
        }

        ImageView btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(v -> {

            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        if (extras.getBoolean("online")) {
                            ArrayList<String> listsArray = new ArrayList<>();

                            CollectionReference docRef = db.collection("templates").document(user.getEmail()).collection(extras.getString("tableName"));
                            docRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        listsArray.add(documentSnapshot.getId());
                                    }
                                    for (String name : listsArray) {
                                        db.collection("templates").document(user.getEmail()).collection(extras.getString("tableName")).document(name).delete();
                                    }
                                    listsArray.clear();
                                }
                            });
                            DocumentReference documentReference = db.collection("templates").document(user.getEmail());
                            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    List<String> array = new ArrayList<>();
                                    if (documentSnapshot.exists()) {
                                        for (String table : (ArrayList<String>) documentSnapshot.get("tables")) {
                                            if (!table.equals(extras.getString("tableName"))) {
                                                array.add(table);
                                            }
                                        }
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("tables", array);
                                        db.collection("templates").document(user.getEmail()).set(map);
                                    }
                                }
                            });
                        } else {
                            database.execSQL("DROP TABLE IF EXISTS " + extras.getString("tableName"));
                            database.delete("table_templates", "table_name = ?", new String[]{replaceSpace(extras.getString("tableName"), false)});
                            System.out.println("Template deleted");
                            database.close();
                        }
                        onBackPressed();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(TemplateInfo.this);
            builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();

        });

        Button btnOpenAddingItemActivity = findViewById(R.id.btnOpenAddingItemActivity);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> onBackPressed());
        btnOpenAddingItemActivity.setOnClickListener(v -> {
            if (extras != null) {
                if (extras.getBoolean("online")) {

                } else {
                    if (!uniqueCheck(templateName.getText().toString())) {
                        return;
                    }

                    ContentValues valuesItem = new ContentValues();
                    valuesItem.put("table_name", templateName.getText().toString());

                    database.update("table_templates", valuesItem, "table_name = ?", new String[]{replaceSpace(extras.getString("tableName"), false)});

                    if (!templateName.getText().toString().equals(replaceSpace(extras.getString("tableName"), false))) {
                        database.execSQL("ALTER TABLE " + extras.getString("tableName") + " RENAME TO " + replaceSpace(templateName.getText().toString(), true));
                    }
                    onBackPressed();
                }

            } else {
                createNewTemplate();
            }
        });

        Button btnDownloadTemplate = findViewById(R.id.btnDownloadTemplate);
        btnDownloadTemplate.setOnClickListener(view -> {
            if (!download) {
                findViewById(R.id.contDownload).setVisibility(View.VISIBLE);
                findViewById(R.id.contCreateNewTemplate).setVisibility(View.GONE);
                btnDownloadTemplate.setText("Create own template");
            } else {
                findViewById(R.id.contDownload).setVisibility(View.GONE);
                findViewById(R.id.contCreateNewTemplate).setVisibility(View.VISIBLE);
                btnDownloadTemplate.setText("Load online template");
            }
            download = !download;
        });

        Button btnDownload = findViewById(R.id.btnDownload);
        btnDownload.setOnClickListener(view -> {
            txtOwner = findViewById(R.id.inputOwnerEmail);
            txtTemplateName = findViewById(R.id.inputTemplateName);

            if (TextUtils.isEmpty(txtOwner.getText().toString())) {
                txtOwner.setError("Email cannot be empty");
                txtOwner.requestFocus();
                return;
            }else{
                String ownerTestSpace = txtOwner.getText().toString();
                if(ownerTestSpace.length() > 3 && ownerTestSpace.charAt(ownerTestSpace.length() - 1) == ' '){
                    String ownerWithoutSpace = ownerTestSpace.substring(0, ownerTestSpace.length() - 1);
                    txtOwner.setText(ownerWithoutSpace);
                }
            }
            if (TextUtils.isEmpty(txtTemplateName.getText().toString())) {
                txtTemplateName.setError("Name cannot be empty");
                txtTemplateName.requestFocus();
                return;
            }

            DocumentReference ownerTest = db.collection("templates").document(txtOwner.getText().toString()).collection(txtTemplateName.getText().toString()).document("table_info");
            ownerTest.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        ArrayList<SingleListAplikator> listsArray = new ArrayList<>();
                        CollectionReference ownerTest2 = db.collection("templates").document(txtOwner.getText().toString()).collection(txtTemplateName.getText().toString());
                        ownerTest2.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (!uniqueCheck(txtTemplateName.getText().toString())) {
                                    return;
                                }

                                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    if (!documentSnapshot.getId().equals("table_info")) {
                                        String name = documentSnapshot.getId();
                                        String color = documentSnapshot.getString("color");
                                        String notes = documentSnapshot.getString("notes");
                                        String type1 = documentSnapshot.getString("category");
                                        int packed = documentSnapshot.getLong("packed").intValue();
                                        int pcs = documentSnapshot.getLong("pcs").intValue();
                                        SingleListAplikator item = new SingleListAplikator(name, notes, type1, packed, pcs, color);
                                        listsArray.add(item);
                                    }
                                }
                                createDownloadedTemplate(listsArray);
                            }
                        });
                    } else {
                        Toast.makeText(TemplateInfo.this, "Incorrect owner’s email or list name.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    private void createNewTemplate() {
        if (!uniqueCheck(templateName.getText().toString())) {
            return;
        }
        database.execSQL("CREATE TABLE " + replaceSpace(templateName.getText().toString(), true) + " ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' TEXT, 'pcs' INTEGER, 'color' TEXT, 'category' TEXT, 'notes' TEXT, 'packed' INTEGER);");

        ContentValues values = new ContentValues();
        values.put("table_name", templateName.getText().toString());
        database.insert("table_templates", null, values);
        onBackPressed();
    }

    private void createDownloadedTemplate(ArrayList<SingleListAplikator> array) {
        String tableNameWithoutSpace = replaceSpace(txtTemplateName.getText().toString(), true);
        database.execSQL("CREATE TABLE " + tableNameWithoutSpace + " ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' TEXT, 'pcs' INTEGER, 'color' TEXT, 'category' TEXT, 'notes' TEXT, 'packed' INTEGER);");

        ContentValues values = new ContentValues();
        values.put("table_name", txtTemplateName.getText().toString());
        database.insert("table_templates", null, values);

        for (SingleListAplikator item : array) {
            ContentValues valuesItem = new ContentValues();
            valuesItem.put("name", item.getName());
            valuesItem.put("pcs", item.getPcs());
            valuesItem.put("category", item.getCategory());
            valuesItem.put("notes", item.getNotes());
            valuesItem.put("packed", item.getPacked());
            valuesItem.put("color", item.getColor());
            database.insert(tableNameWithoutSpace, null, valuesItem);
        }
        onBackPressed();
    }

    //Kontrola unikátnosti názvu tabulky v dámci lokální databáze
    private boolean uniqueCheck(String name) {
        if (TextUtils.isEmpty(name)) {
            templateName.setError("Name cannot be empty");
            templateName.requestFocus();
            return false;
        }

        if (name.contains("_")) {
            Toast.makeText(this, "Name of the journey cant contain _ characters", Toast.LENGTH_LONG).show();
            return false;
        }

        if (extras != null) {
            if (replaceSpace(name, true).equals(extras.getString("tableName"))) {
                return true;
            }
        }
        Cursor cursor = database.rawQuery("SELECT * FROM table_templates WHERE table_name = ?", new String[]{name});

        if (cursor.getCount() > 0) {
            Toast.makeText(this, "Name of the template is already used", Toast.LENGTH_LONG).show();
            return false;
        }
        cursor = database.rawQuery("SELECT * FROM table_lists WHERE table_name = ?", new String[]{name});
        if (cursor.getCount() > 0) {
            Toast.makeText(this, "Name of the template is already used", Toast.LENGTH_LONG).show();
            return false;
        }
        cursor = database.rawQuery("SELECT * FROM table_journal WHERE name = ?", new String[]{name});
        if (cursor.getCount() > 0) {
            Toast.makeText(this, "Name of the template is already used", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;

    }

    private String replaceSpace(String name, boolean replaceSpace) {
        if (!replaceSpace) {
            return name.replace("_", " ");
        }
        return name.replace(" ", "_");
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(TemplateInfo.this, Templates.class);
        startActivity(intent);
        finish();
    }
}
