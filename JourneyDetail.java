package com.easierlifeapps.easypacking;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.easierlifeapps.easypacking.Adapters.AdapterJourney;
import com.easierlifeapps.easypacking.Adapters.DatabaseOpenHelper;
import com.easierlifeapps.easypacking.Objects.Journey;

import java.util.ArrayList;
import java.util.Calendar;

public class JourneyDetail extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{
    TextView dateStart, dateEnd;
    boolean startPicker, edit;
    int startNum, endNum;
    DatabaseOpenHelper openHelper;
    SQLiteDatabase database;
    Bundle extras;
    EditText txtName;
    EditText txtLocation;
    EditText txtNotes;
    Button btnDeleteJourney;
    /**
     * Aktivita sloužící k zobrazení detailu cesty společně s příspěvky v ní
     * Zde může uživatel provádě i změny v informacích o cetě*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_detail);
        extras = getIntent().getExtras();
        edit = false;

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        openHelper = new DatabaseOpenHelper(this);
        database = openHelper.getReadableDatabase();

        dateStart = findViewById(R.id.pickStart);
        dateEnd = findViewById(R.id.pickEnd);


        dateStart.setOnClickListener(v->{
            if(edit){
                startPicker = true;
                showDatePickerDialog();
            }
        });

        dateEnd.setOnClickListener(v->{
            if(edit){
                startPicker = false;
                showDatePickerDialog();
            }
        });

        txtName = findViewById(R.id.inputName);
        txtLocation = findViewById(R.id.inputLocation);
        txtNotes = findViewById(R.id.inputNotes);

        txtName.setFocusable(false);
        txtLocation.setFocusable(false);
        txtNotes.setFocusable(false);

        ImageView btnSave = findViewById(R.id.btnEditToolbar);
        btnSave.setOnClickListener(v->{
            if(edit){
                String name = txtName.getText().toString();
                String location = txtLocation.getText().toString();
                String notes = txtNotes.getText().toString();
                String start = this.dateStart.getText().toString();
                String end = this.dateEnd.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    txtName.setError("Name cannot be empty");
                    txtName.requestFocus();
                } else if (TextUtils.isEmpty(location)) {
                    txtLocation.setError("Location cannot be empty");
                    txtLocation.requestFocus();
                }else if (start.equals("Pick start date")) {
                    dateStart.setError("Choose start date");
                    dateStart.requestFocus();
                }else if (end.equals("Pick end date")) {
                    dateEnd.setError("Choose end date");
                    dateEnd.requestFocus();
                }else{
                    if(checkDates(start, end) && uniqueCheck(name)){
                        ContentValues values = new ContentValues();
                        values.put("name", name);
                        values.put("start", start);
                        values.put("end", end);
                        values.put("location", location);
                        values.put("notes", notes);
                        database.update("table_journal", values, "name = ?", new String[]{replaceSpace(extras.getString("tableName"), false)});

                        if(!replaceSpace(name, true).equals(extras.getString("tableName"))){
                            database.execSQL("ALTER TABLE " + extras.getString("tableName") + " RENAME TO " + replaceSpace(name, true));
                        }
                        setEditableJourney(false);
                        Toast.makeText(JourneyDetail.this, "Changes saved", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(JourneyDetail.this, "Check picked dates!", Toast.LENGTH_LONG).show();
                    }
                }
            }else{
                setEditableJourney(true);
                Toast.makeText(JourneyDetail.this, "Journey is now editable", Toast.LENGTH_LONG).show();
            }

        });

        findViewById(R.id.btnAddPost).setOnClickListener(v->{
            Intent intent = new Intent(JourneyDetail.this, Post.class);
            intent.putExtra("tableName", extras.getString("tableName"));
            startActivity(intent);
            finish();
        });

        btnDeleteJourney = findViewById(R.id.btnDeleteJourney);
        btnDeleteJourney.setVisibility(View.GONE);
        btnDeleteJourney.setOnClickListener(v->{
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        database.execSQL("DROP TABLE IF EXISTS " + extras.getString("tableName"));
                        database.delete("table_journal", "name = ?", new String[]{replaceSpace(extras.getString("tableName"), false)});
                        System.out.println("Journey deleted");
                        database.close();
                        onBackPressed();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(JourneyDetail.this);
            builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        });

        setContentJourney();
        LoadData();
    }

    private void setContentJourney(){
        System.out.println(replaceSpace(extras.getString("tableName"), false));
        Cursor cursor = database.rawQuery("SELECT * FROM table_journal WHERE name = ?", new String[]{replaceSpace(extras.getString("tableName"), false)});
        while (cursor.moveToNext()){
            txtName.setText(cursor.getString(1));
            dateStart.setText(cursor.getString(2));
            dateEnd.setText(cursor.getString(3));
            txtLocation.setText(cursor.getString(4));
            txtNotes.setText(cursor.getString(5));
        }

    }

    public void showDatePickerDialog(){
        DatePickerDialog startDate = new DatePickerDialog(this, this, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        startDate.show();
    }

    private void setEditableJourney(boolean set){
        txtName.setFocusable(set);
        txtLocation.setFocusable(set);
        txtNotes.setFocusable(set);
        if(set){
            txtName.setFocusableInTouchMode(true);
            txtLocation.setFocusableInTouchMode(true);
            txtNotes.setFocusableInTouchMode(true);
            btnDeleteJourney.setVisibility(View.VISIBLE);
        }else {
            btnDeleteJourney.setVisibility(View.GONE);
        }
        edit = set;
    }

    private boolean uniqueCheck(String name){
        String tableName = extras.getString("tableName");
        if(tableName.equals(replaceSpace(name, true))){
            return true;
        }

        Cursor cursor = database.rawQuery("SELECT * FROM table_journal WHERE name = ?", new String[]{name});

        if(cursor.getCount() > 0){
            Toast.makeText(this, "Name of the item is already used", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;

    }

    private boolean checkDates(String start, String end){
        String[] partsStart = start.split("\\.");
        String[] partsEnd = end.split("\\.");

        int resultStart = Integer.parseInt(partsStart[2] + partsStart[1] + partsStart[0] + "");
        int resultEnd = Integer.parseInt(partsEnd[2] + partsEnd[1] + partsEnd[0] + "");
        return resultStart <= resultEnd;
    }
    private void LoadData() {
        Cursor cursor = database.rawQuery("SELECT * FROM " + extras.getString("tableName"), null);

        ArrayList<Journey> listsArray = new ArrayList<>();
        while (cursor.moveToNext()) {
            listsArray.add(new Journey(cursor.getString(1), cursor.getString(2)));
        }

        FillRecycler(listsArray);
    }

    private void FillRecycler(final ArrayList<Journey> array) {
        RecyclerView recyclerView = findViewById(R.id.listJourneys);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        AdapterJourney adapterLists = new AdapterJourney(array);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapterLists);
        adapterLists.setOnItemClickListener(position -> {
            Intent intent;
            intent = new Intent(JourneyDetail.this, Post.class);
            intent.putExtra("tableName", extras.getString("tableName"));
            intent.putExtra("post", array.get(position).getName());
            startActivity(intent);
            finish();
        });
    }

    private String replaceSpace(String name, boolean replaceSpace) {
        if (!replaceSpace) {
            return name.replace("_", " ");
        }
        return name.replace(" ", "_");
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(JourneyDetail.this, Journal.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        int month = i1 + 1;
        String date;
        String dateNum;
        if(month < 10){
            date = i2 + ".0" + month + "." + i;
            dateNum = i2 + "0" + month + i + "";
        }else {
            date = i2 + "." + month + "." + i;
            dateNum = i2 + "" + month + i + "";
        }

        if (startPicker) {
            startNum = Integer.parseInt(dateNum);
            dateStart.setText(date);
        } else {
            endNum = Integer.parseInt(dateNum);
            dateEnd.setText(date);
        }
    }
}
