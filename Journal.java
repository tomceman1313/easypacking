package com.easierlifeapps.easypacking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.easierlifeapps.easypacking.Adapters.AdapterJournal;
import com.easierlifeapps.easypacking.Adapters.DatabaseOpenHelper;
import com.easierlifeapps.easypacking.Objects.Journey;

import java.util.ArrayList;

public class Journal extends AppCompatActivity {

    DatabaseOpenHelper openHelper;
    SQLiteDatabase database;
    /**
     * Aktivita zobrazující vytvořené cesty v cestovním deníku*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);
        findViewById(R.id.btnEditToolbar).setVisibility(View.GONE);
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        openHelper = new DatabaseOpenHelper(this);
        database = openHelper.getReadableDatabase();

        ImageButton addList = findViewById(R.id.btnAddJourney);
        addList.setOnClickListener(v -> {
            Intent lists = new Intent(Journal.this, JourneyInfo.class);
            lists.putExtra("tableName", "new");
            startActivity(lists);
            finish();
        });
        LoadData();
    }

    private void LoadData() {
        Cursor cursor = database.rawQuery("SELECT * FROM table_journal", null);

        ArrayList<Journey> listsArray = new ArrayList<>();
        while (cursor.moveToNext()) {

            listsArray.add(new Journey(cursor.getString(1), cursor.getString(2) + " - " + cursor.getString(3)));
        }

        FillRecycler(listsArray);
    }

    private void FillRecycler(final ArrayList<Journey> array) {
        RecyclerView recyclerView = findViewById(R.id.listJourneys);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        AdapterJournal adapterLists = new AdapterJournal(array);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapterLists);
        adapterLists.setOnItemClickListener(position -> {
            Intent intent;
            intent = new Intent(Journal.this, JourneyDetail.class);
            intent.putExtra("tableName", replaceSpace(array.get(position).getName(), true));
            startActivity(intent);
            finish();
        });
    }

    private String replaceSpace(String name, boolean replaceSpace) {
        if (!replaceSpace) {
            return name.replace("_", "\\s");
        }
        return name.replace(" ", "_");
    }

    @Override
    public void onBackPressed() {
        Intent main = new Intent(Journal.this, MainActivity.class);
        startActivity(main);
        finish();
    }
}
