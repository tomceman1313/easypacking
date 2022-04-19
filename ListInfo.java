package com.easierlifeapps.easypacking;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

public class ListInfo extends AppCompatActivity {
    int online;
    boolean group;
    EditText txtDays, txtName, txtLocation, txtOwner, txtListName;
    Bundle extras;
    List<String> arrayTemplates = new ArrayList<>();
    DatabaseOpenHelper openHelper;
    SQLiteDatabase database;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser user;

    /**
     * Aktivita sloužící k vytvoření nového seznamu a zároveň k zobrazení detailu existujícího
     * Detail zobrazuje jak pro lokální tak i cloudové seznamy*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_info);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if(user == null){
            startActivityForResult(new Intent(ListInfo.this, Login.class), 1);
        }

        Button btnSave = findViewById(R.id.btnOpenAddingItemActivity);
        ImageView btnBack = findViewById(R.id.btnBack);
        ImageButton btnDayPlus = findViewById(R.id.btnDayPlus);
        ImageButton btnDayMinus = findViewById(R.id.btnDayMinus);

        txtName = findViewById(R.id.inputName);
        txtLocation = findViewById(R.id.txtLocation);

        final Button btnOffline = findViewById(R.id.buttonSingle);
        final Button btnOnline = findViewById(R.id.buttonGroup);

        final Button btnJoinList = findViewById(R.id.btnJoinList);

        txtDays = findViewById(R.id.txtDays);
        online = 0;
        group = false;
        extras = getIntent().getExtras();
        openHelper = new DatabaseOpenHelper(this);
        database = openHelper.getWritableDatabase();

        if ((extras.getString("tableName").equals("new"))) {
            findViewById(R.id.btnDelete).setVisibility(View.GONE);
        }
        findViewById(R.id.contJoin).setVisibility(View.GONE);

        ImageView btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(v -> {
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        if(extras.getBoolean("online")){
                            ArrayList<String> listsArray = new ArrayList<>();

                            CollectionReference docRef = db.collection("lists").document(user.getEmail()).collection(extras.getString("tableName"));
                            docRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
                                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    listsArray.add(documentSnapshot.getId());
                                }
                                for(String name : listsArray){
                                    db.collection("lists").document(user.getEmail()).collection(extras.getString("tableName")).document(name).delete();
                                }

                            });
                            DocumentReference documentReference = db.collection("lists").document(user.getEmail());
                            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    List<String> array = new ArrayList<>();
                                    if (documentSnapshot.exists()) {
                                        for (String table : (ArrayList<String>) documentSnapshot.get("tables")) {
                                            if(!table.equals(extras.getString("tableName"))){
                                                array.add(table);
                                            }
                                        }
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("tables", array);
                                        db.collection("lists").document(user.getEmail()).set(map);
                                    }
                                }
                            });
                        }else {
                            database.execSQL("DROP TABLE IF EXISTS " + extras.getString("tableName"));
                            database.delete("table_lists", "table_name = ?", new String[]{replaceSpace(extras.getString("tableName"), false)});
                            System.out.println("List deleted");
                            database.close();
                        }
                        onBackPressed();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(ListInfo.this);
            builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        });

        btnBack.setOnClickListener(v -> onBackPressed());

        btnSave.setOnClickListener(v -> {
            if ((extras.getString("tableName").equals("new"))) {
                createNewList();
            } else {
                String name = txtName.getText().toString();

                if(!uniqueCheck(name)){
                    return;
                }

                if(extras.getBoolean("online")){
                    Map<String, Object> updatedList = new HashMap<>();
                    updatedList.put("days", Integer.parseInt(txtDays.getText().toString()));
                    updatedList.put("location", txtLocation.getText().toString());

                    if(extras.getString("path") != null){
                        updatedList.put("path", "");
                        db.collection("lists").document(extras.getString("path")).collection(extras.getString("tableName")).document("table_info").set(updatedList);
                    }else {
                        updatedList.put("path", "");
                        db.collection("lists").document(user.getEmail()).collection(extras.getString("tableName")).document("table_info").set(updatedList);
                    }

                }else {
                    ContentValues valuesItem = new ContentValues();
                    valuesItem.put("table_name", name);
                    valuesItem.put("location", txtLocation.getText().toString());
                    valuesItem.put("days", txtDays.getText().toString());

                    database.update("table_lists", valuesItem, "table_name = ?", new String[]{replaceSpace(extras.getString("tableName"), false)});

                    if(!replaceSpace(name, true).equals(extras.getString("tableName"))){
                        database.execSQL("ALTER TABLE " + extras.getString("tableName") + " RENAME TO " + replaceSpace(name, true));
                    }
                }
                System.out.println("TABLE UPDATED");
                onBackPressed();
            }
        });

        if(!extras.getString("tableName").equals("new")){
            findViewById(R.id.textView4).setVisibility(View.GONE);
            findViewById(R.id.linearLayout).setVisibility(View.GONE);
            findViewById(R.id.textView8).setVisibility(View.GONE);
            findViewById(R.id.linearLayout4).setVisibility(View.GONE);
            findViewById(R.id.constraintLayout2).setVisibility(View.GONE);
            findViewById(R.id.btnShareList).setVisibility(View.VISIBLE);

            ImageView btnShare = findViewById(R.id.btnShareList);
            if (extras.getBoolean("online")) {
                btnShare.setVisibility(View.GONE);
            }

            btnShare.setOnClickListener(v -> {
                Map<String, Object> updatedList = new HashMap<>();
                updatedList.put("days", Integer.parseInt(txtDays.getText().toString()));
                updatedList.put("location", txtLocation.getText().toString());
                updatedList.put("path", "");
                db.collection("lists").document(user.getEmail()).collection(txtName.getText().toString()).document("table_info").set(updatedList);

                DocumentReference documentReference = db.collection("lists").document(user.getEmail());
                documentReference.get().addOnSuccessListener(documentSnapshot -> {
                    List<String> array = new ArrayList<>();
                    if (documentSnapshot.exists()) {
                        for (String table : (ArrayList<String>) documentSnapshot.get("tables")) {
                            array.add(table);
                            if(table.equals(txtName.getText().toString())){
                                Toast.makeText(ListInfo.this, "List of this name is already shared.", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                        array.add(txtName.getText().toString());
                        Map<String, Object> map = new HashMap<>();
                        map.put("tables", array);
                        db.collection("lists").document(user.getEmail()).set(map);
                    }else {
                        array.add(txtName.getText().toString());
                        Map<String, Object> map = new HashMap<>();
                        map.put("tables", array);
                        db.collection("lists").document(user.getEmail()).set(map);
                    }
                    Cursor cursor = database.rawQuery("SELECT * FROM " + replaceSpace(txtName.getText().toString(), true), null);
                    while (cursor.moveToNext()){
                        Map<String, Object> valuesItem = new HashMap<>();
                        valuesItem.put("pcs", cursor.getInt(2));
                        valuesItem.put("category", cursor.getString(4));
                        valuesItem.put("notes", cursor.getString(5));
                        valuesItem.put("packed", 0);
                        valuesItem.put("color", cursor.getString(3));
                        db.collection("lists").document(user.getEmail()).collection(txtName.getText().toString()).document(cursor.getString(1)).set(valuesItem);
                    }

                    Toast.makeText(ListInfo.this, "List shared", Toast.LENGTH_LONG).show();
                });
            });
            btnSave.setText("Save");

            if(extras.getBoolean("online")){
                DocumentReference documentReference;
                if(extras.getString("path") != null){
                    documentReference = db.collection("lists").document(extras.getString("path")).collection(extras.getString("tableName")).document("table_info");
                }else {
                    documentReference = db.collection("lists").document(user.getEmail()).collection(extras.getString("tableName")).document("table_info");
                }
                documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            txtDays.setText(documentSnapshot.get("days").toString());
                            txtLocation.setText(documentSnapshot.get("location").toString());
                            txtName.setText(extras.getString("tableName"));
                            txtName.setFocusable(false);
                        }
                    }
                });
            }else {
                Cursor cursor = database.rawQuery("SELECT * FROM table_lists WHERE table_name = ?", new String[]{replaceSpace(extras.getString("tableName"), false)});
                cursor.moveToNext();

                txtDays.setText(cursor.getString(4));
                txtLocation.setText(cursor.getString(3));
                txtName.setText(cursor.getString(1));
            }

        }else{
            btnOffline.setOnClickListener(v -> {
                online = 0;
                btnOffline.setBackgroundResource(R.color.colorPrimaryDark);
                btnOnline.setBackgroundResource(R.color.white);
                btnOnline.setTextColor(Color.parseColor("#525252"));
                btnOffline.setTextColor(Color.parseColor("#FFFFFF"));
            });

            btnOnline.setOnClickListener(v -> {
                online = 1;
                btnOnline.setBackgroundResource(R.color.colorPrimaryDark);
                btnOffline.setBackgroundResource(R.color.white);
                btnOffline.setTextColor(Color.parseColor("#525252"));
                btnOnline.setTextColor(Color.parseColor("#FFFFFF"));
            });

            btnJoinList.setOnClickListener(v -> {
                if(!group){
                    findViewById(R.id.contJoin).setVisibility(View.VISIBLE);
                    findViewById(R.id.contNewList).setVisibility(View.GONE);
                    btnJoinList.setText("Create own list");
                }else {
                    findViewById(R.id.contJoin).setVisibility(View.GONE);
                    findViewById(R.id.contNewList).setVisibility(View.VISIBLE);
                    btnJoinList.setText("Join someone's list");
                }
                group = !group;
            });

            Button btnJoin = findViewById(R.id.btnJoin);
            btnJoin.setOnClickListener(view ->{
                //Funkčnost poskytující připojení se ke skupinovému seznamu
                txtOwner = findViewById(R.id.inputOwnerEmail);
                txtListName = findViewById(R.id.inputListName);

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
                if (TextUtils.isEmpty(txtListName.getText().toString())) {
                    txtListName.setError("Name cannot be empty");
                    txtListName.requestFocus();
                    return;
                }

                DocumentReference ownerTest = db.collection("lists").document(txtOwner.getText().toString()).collection(txtListName.getText().toString()).document("table_info");
                ownerTest.get().addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists() && !txtOwner.getText().toString().equals(user.getEmail())){
                        Map<String, Object> updatedList = new HashMap<>();
                        updatedList.put("days", Integer.parseInt(txtDays.getText().toString()));
                        updatedList.put("location", txtLocation.getText().toString());
                        updatedList.put("path", txtOwner.getText().toString());
                        if(documentSnapshot.get("path").equals("")){
                            db.collection("lists").document(user.getEmail()).collection(txtListName.getText().toString()).document("table_info").set(updatedList);
                            DocumentReference documentReference = db.collection("lists").document(user.getEmail());
                            documentReference.get().addOnSuccessListener(documentSnapshot1 -> {
                                List<String> array = new ArrayList<>();
                                if (documentSnapshot1.exists()) {
                                    for (String table : (ArrayList<String>) documentSnapshot1.get("tables")) {
                                        array.add(table);
                                        if (table.equals(txtListName.getText().toString())){
                                            Toast.makeText(ListInfo.this, "Name of the list is already used.", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                    }
                                    array.add(txtListName.getText().toString());
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("tables", array);
                                    db.collection("lists").document(user.getEmail()).set(map);
                                }else {
                                    array.add(txtListName.getText().toString());
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("tables", array);
                                    db.collection("lists").document(user.getEmail()).set(map);
                                }
                                onBackPressed();
                            });
                        }else{
                            Toast.makeText(ListInfo.this, "Incorrect owner’s email.", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(ListInfo.this, "Incorrect owner’s email or list name.", Toast.LENGTH_SHORT).show();
                    }
                });

            });

            Button btnDownload = findViewById(R.id.btnDownloadList);
            btnDownload.setOnClickListener(view -> {
                //Funkce umožňující stažení zvoleného seznamu
                txtOwner = findViewById(R.id.inputOwnerEmail);
                txtListName = findViewById(R.id.inputListName);

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
                if (TextUtils.isEmpty(txtListName.getText().toString())) {
                    txtListName.setError("Name cannot be empty");
                    txtListName.requestFocus();
                    return;
                }

                DocumentReference ownerTest = db.collection("lists").document(txtOwner.getText().toString()).collection(txtListName.getText().toString()).document("table_info");
                ownerTest.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            ArrayList<SingleListAplikator> listsArray = new ArrayList<>();
                            CollectionReference ownerTest2 = db.collection("lists").document(txtOwner.getText().toString()).collection(txtListName.getText().toString());
                            ownerTest2.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    if (!uniqueCheck(txtListName.getText().toString())) {
                                        return;
                                    }
                                    int days = 0;
                                    String location = "";
                                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        if (!documentSnapshot.getId().equals("table_info")) {
                                            String name = documentSnapshot.getId();
                                            String color = documentSnapshot.getString("color");
                                            String notes = documentSnapshot.getString("notes");
                                            String type = documentSnapshot.getString("category");
                                            int packed = documentSnapshot.getLong("packed").intValue();
                                            int pcs = documentSnapshot.getLong("pcs").intValue();
                                            SingleListAplikator item = new SingleListAplikator(name, notes, type, packed, pcs, color);
                                            listsArray.add(item);
                                        }else{
                                            days = Integer.parseInt(documentSnapshot.get("days").toString());
                                            location = documentSnapshot.get("location").toString();
                                        }
                                    }
                                    createDownloadedList(listsArray, days, location);
                                }
                            });
                        } else {
                            Toast.makeText(ListInfo.this, "Name or owner is incorrect", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            });

            loadTemplates();
        }

        btnDayMinus.setOnClickListener(v -> {
            if(txtDays.getText().toString().equals("0")){
                return;
            }
            int daysCounter = Integer.parseInt(txtDays.getText().toString());
            txtDays.setText(String.valueOf(daysCounter - 1));
        });

        btnDayPlus.setOnClickListener(v -> {
            int daysCounter = Integer.parseInt(txtDays.getText().toString());
            txtDays.setText(String.valueOf(daysCounter + 1));
        });
    }
    /**
     * Metoda sloužící k načtení dostupných šablon a zobrazení jich pro možnost zvolení*/
    private void loadTemplates(){
        LinearLayout oddLayout = findViewById(R.id.buttonsOdd);
        LinearLayout evenLayout = findViewById(R.id.buttonsEven);

        Cursor cursor = database.rawQuery("SELECT * FROM table_templates", null);

        int i = 0;
        while (cursor.moveToNext()){
            Button button = (Button)getLayoutInflater().inflate(R.layout.template_button, null);
            RelativeLayout space = new RelativeLayout(this);
            space.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 8));
            button.setText(cursor.getString(1));
            button.setOnClickListener(v -> {
                Button btn = (Button) v;
                String btnName = btn.getText().toString();
                List<String> arrayWithoutTemplate = new ArrayList<>();
                if(arrayTemplates.contains(btnName)){
                    arrayTemplates.add(btn.getText().toString());
                    btn.setBackgroundResource(R.drawable.border_dark);
                    btn.setTextColor(Color.parseColor("#525252"));
                    for(int p = 0; p < arrayTemplates.size(); p++){
                        if(btnName.equals(arrayTemplates.get(p))){
                            continue;
                        }
                        arrayWithoutTemplate.add(arrayTemplates.get(p));
                    }
                    arrayTemplates = arrayWithoutTemplate;

                }else{
                    arrayTemplates.add(btn.getText().toString());
                    btn.setBackgroundResource(R.color.colorPrimaryDark);
                    btn.setTextColor(Color.parseColor("#FFFFFF"));
                }
            });
            if ((i % 2) == 0) {
                oddLayout.addView(button);
                oddLayout.addView(space);
            } else {
                evenLayout.addView(button);
                evenLayout.addView(space);
            }
            ++i;
        }

    }

    private void createNewList(){
        if(!uniqueCheck(txtName.getText().toString())){
            return;
        }

        if(online == 1){
            Map<String, Object> updatedList = new HashMap<>();
            updatedList.put("days", Integer.parseInt(txtDays.getText().toString()));
            updatedList.put("location", txtLocation.getText().toString());
            updatedList.put("path", "");
            db.collection("lists").document(user.getEmail()).collection(txtName.getText().toString()).document("table_info").set(updatedList);

            DocumentReference documentReference = db.collection("lists").document(user.getEmail());
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    List<String> array = new ArrayList<>();
                    if (documentSnapshot.exists()) {
                        for (String table : (ArrayList<String>) documentSnapshot.get("tables")) {
                            array.add(table);
                        }
                        array.add(txtName.getText().toString());
                        Map<String, Object> map = new HashMap<>();
                        map.put("tables", array);
                        db.collection("lists").document(user.getEmail()).set(map);
                    }else {
                        array.add(txtName.getText().toString());
                        Map<String, Object> map = new HashMap<>();
                        map.put("tables", array);
                        db.collection("lists").document(user.getEmail()).set(map);
                    }
                }
            });

            for(String template : arrayTemplates){
                Cursor cursor = database.rawQuery("SELECT * FROM " + replaceSpace(template,true), null);
                while (cursor.moveToNext()){
                    if(uniqueItem(cursor.getString(1), txtName.getText().toString())){
                        Map<String, Object> valuesItem = new HashMap<>();
                        valuesItem.put("pcs", cursor.getInt(2));
                        valuesItem.put("category", cursor.getString(4));
                        valuesItem.put("notes", cursor.getString(5));
                        valuesItem.put("packed", cursor.getInt(6));
                        valuesItem.put("color", cursor.getString(3));
                        db.collection("lists").document(user.getEmail()).collection(txtName.getText().toString()).document(cursor.getString(1)).set(valuesItem);
                    }
                }
            }
            onBackPressed();
        }else {
            String nameOfTableWithoutSpace = replaceSpace(txtName.getText().toString(), true);
            database.execSQL("CREATE TABLE " + nameOfTableWithoutSpace +" ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' TEXT, 'pcs' INTEGER, 'color' TEXT, 'category' TEXT, 'notes' TEXT, 'packed' INTEGER);");

            ContentValues values = new ContentValues();
            values.put("table_name", txtName.getText().toString());
            values.put("online", online);
            values.put("location", txtLocation.getText().toString());
            values.put("days", txtDays.getText().toString());
            database.insert("table_lists", null, values);

            for(String template : arrayTemplates){
                Cursor cursor = database.rawQuery("SELECT * FROM " + template, null);
                while (cursor.moveToNext()){
                    if(uniqueItem(cursor.getString(1), replaceSpace(txtName.getText().toString(), true))){
                        ContentValues valuesItem = new ContentValues();
                        valuesItem.put("name", cursor.getString(1));
                        valuesItem.put("pcs", cursor.getInt(2));
                        valuesItem.put("category", cursor.getString(4));
                        valuesItem.put("notes", cursor.getString(5));
                        valuesItem.put("packed", cursor.getInt(6));
                        valuesItem.put("color", cursor.getString(3));
                        database.insert(nameOfTableWithoutSpace, null, valuesItem);
                    }
                }
            }
            onBackPressed();
        }
    }

    private void createDownloadedList(ArrayList<SingleListAplikator> array, int days, String location) {
        String tableNameWithoutSpace = replaceSpace(txtListName.getText().toString(), true);
        database.execSQL("CREATE TABLE " + tableNameWithoutSpace +" ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' TEXT, 'pcs' INTEGER, 'color' TEXT, 'category' TEXT, 'notes' TEXT, 'packed' INTEGER);");

        ContentValues values = new ContentValues();
        values.put("table_name", txtListName.getText().toString());
        values.put("online", online);
        values.put("location", location);
        values.put("days", days);
        database.insert("table_lists", null, values);

        for (SingleListAplikator item : array) {
            ContentValues valuesItem = new ContentValues();
            valuesItem.put("name", item.getName());
            valuesItem.put("pcs", item.getPcs());
            valuesItem.put("category", item.getCategory());
            valuesItem.put("color", item.getColor());
            valuesItem.put("notes", item.getNotes());
            valuesItem.put("packed", item.getPacked());
            database.insert(tableNameWithoutSpace, null, valuesItem);
        }
        onBackPressed();
    }

    private boolean uniqueCheck(String name){
        if (TextUtils.isEmpty(name)) {
            txtName.setError("Name cannot be empty");
            txtName.requestFocus();
            return false;
        }

        if(online == 1 || extras.getBoolean("online")){
           return true;
        }else {
            String tableName = extras.getString("tableName");
            if(tableName.equals(replaceSpace(name, true))){
                return true;
            }

            Cursor cursor = database.rawQuery("SELECT * FROM table_lists WHERE table_name = ?", new String[]{name});

            if (cursor.getCount() > 0) {
                Toast.makeText(this, "Name of the list is already used", Toast.LENGTH_LONG).show();
                return false;
            }
            cursor = database.rawQuery("SELECT * FROM table_templates WHERE table_name = ?", new String[]{name});
            if (cursor.getCount() > 0) {
                Toast.makeText(this, "Name of the list is already used", Toast.LENGTH_LONG).show();
                return false;
            }
            cursor = database.rawQuery("SELECT * FROM table_journal WHERE name = ?", new String[]{name});
            if (cursor.getCount() > 0) {
                Toast.makeText(this, "Name of the list is already used", Toast.LENGTH_LONG).show();
                return false;
            }


        }
        return true;

    }

    private boolean uniqueItem(String itemName, String tableName){
        if(online == 1){
            return true;
        }

        Cursor cursor = database.rawQuery("SELECT * FROM " + tableName + " WHERE name = ?", new String[]{itemName});

        if(cursor.getCount() > 0){
            //Toast.makeText(this, "Name of the item is already used", Toast.LENGTH_LONG).show();
            System.out.println(itemName + " is already in the list");
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
        Intent intent = new Intent(ListInfo.this, Lists.class);
        database.close();
        startActivity(intent);
        finish();
    }
}
