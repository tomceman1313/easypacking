package com.easierlifeapps.easypacking;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.ContentValues;
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

import com.easierlifeapps.easypacking.Adapters.DatabaseOpenHelper;

import java.util.Calendar;

public class JourneyInfo extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    TextView dateStart, dateEnd;
    boolean startPicker;
    int startNum, endNum;
    DatabaseOpenHelper openHelper;
    SQLiteDatabase database;
    /**
     * Aktivita sloužící k vytvoření nové cesty v deníku*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey);

        findViewById(R.id.btnEditToolbar).setVisibility(View.GONE);
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        openHelper = new DatabaseOpenHelper(this);
        database = openHelper.getReadableDatabase();

        dateStart = findViewById(R.id.pickStart);
        dateEnd = findViewById(R.id.pickEnd);

        dateStart.setOnClickListener(v -> {
            startPicker = true;
            showDatePickerDialog();
        });

        dateEnd.setOnClickListener(v -> {
            startPicker = false;
            showDatePickerDialog();
        });

        EditText txtName = findViewById(R.id.inputName);
        EditText txtLocation = findViewById(R.id.inputLocation);
        EditText txtNotes = findViewById(R.id.inputNotes);

        Button btnSave = findViewById(R.id.btnSaveJourney);
        btnSave.setOnClickListener(v -> {
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
            } else if (start.equals("Pick start date")) {
                dateStart.setError("Choose start date");
                dateStart.requestFocus();
            } else if (end.equals("Pick end date")) {
                dateEnd.setError("Choose end date");
                dateEnd.requestFocus();
            } else {
                if (checkDates(start, end) && uniqueCheck(txtName.getText().toString())) {
                    String nameOfTable = replaceSpace(txtName.getText().toString(), true);
                    database.execSQL("CREATE TABLE " + nameOfTable + " ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'title' TEXT, 'content' TEXT);");
                    ContentValues values = new ContentValues();
                    values.put("name", name);
                    values.put("start", start);
                    values.put("end", end);
                    values.put("location", location);
                    values.put("notes", notes);
                    database.insert("table_journal", null, values);
                    Intent intent = new Intent(JourneyInfo.this, Journal.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(JourneyInfo.this, "Check picked dates!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private String replaceSpace(String name, boolean replaceSpace) {
        if (!replaceSpace) {
            return name.replace("_", " ");
        }
        return name.replace(" ", "_");
    }

    public void showDatePickerDialog() {
        DatePickerDialog startDate = new DatePickerDialog(this, this, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        startDate.show();
    }

    private boolean uniqueCheck(String name) {
        if (name.contains("_")) {
            Toast.makeText(this, "Name of the journey cant contain _ characters", Toast.LENGTH_LONG).show();
            return false;
        }

        Cursor cursor = database.rawQuery("SELECT * FROM table_templates WHERE table_name = ?", new String[]{name});

        if (cursor.getCount() > 0) {
            Toast.makeText(this, "Name of the journey is already used", Toast.LENGTH_LONG).show();
            return false;
        }
        cursor = database.rawQuery("SELECT * FROM table_lists WHERE table_name = ?", new String[]{name});
        if (cursor.getCount() > 0) {
            Toast.makeText(this, "Name of the journey is already used", Toast.LENGTH_LONG).show();
            return false;
        }
        cursor = database.rawQuery("SELECT * FROM table_journal WHERE name = ?", new String[]{name});
        if (cursor.getCount() > 0) {
            Toast.makeText(this, "Name of the journey is already used", Toast.LENGTH_LONG).show();
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(JourneyInfo.this, Journal.class);
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
