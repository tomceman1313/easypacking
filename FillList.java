package com.easierlifeapps.easypacking;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.easierlifeapps.easypacking.Adapters.AdapterFillList;
import com.easierlifeapps.easypacking.Adapters.AdapterPackedItems;
import com.easierlifeapps.easypacking.Adapters.DatabaseOpenHelper;
import com.easierlifeapps.easypacking.Adapters.SingleListAplikator;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class FillList extends AppCompatActivity {

    TabLayout tabLayout;
    int packed;
    boolean isPacked;
    Bundle extras;
    String type = "clothes";
    Button btnPacked, btnUnpacked;
    boolean isTemplate;
    /**
     *Aktivita sloužící pro práce uvitř offline seznamu a šablony
     * Zobrazuje položky ve zvoleném listu a je možné jim měnit stav nebo v případě šablony je odmazávat
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_list);

        extras = getIntent().getExtras();
        isTemplate = extras.getBoolean("template");
        isPacked = false;

        TextView txtName = findViewById(R.id.txtListName);
        txtName.setText(replaceSpace(extras.getString("tableName"), false));

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        ImageView btnEdit = findViewById(R.id.btnEditToolbar);
        btnEdit.setOnClickListener(v -> {
            Intent intent;
            if(isTemplate){
                intent = new Intent(FillList.this, TemplateInfo.class);
            }else{
                intent = new Intent(FillList.this, ListInfo.class);
            }
            intent.putExtra("tableName", extras.getString("tableName"));
            intent.putExtra("online", false);
            startActivity(intent);
            finish();
        });

        tabLayout = findViewById(R.id.tab_layout);

        tabLayout.addTab(tabLayout.newTab().setText("Clothes"));
        tabLayout.addTab(tabLayout.newTab().setText("Hygiene"));
        tabLayout.addTab(tabLayout.newTab().setText("Health"));
        tabLayout.addTab(tabLayout.newTab().setText("Documents"));
        tabLayout.addTab(tabLayout.newTab().setText("Devices"));
        tabLayout.addTab(tabLayout.newTab().setText("Other"));
        tabLayout.addTab(tabLayout.newTab().setText("Food"));

        tabLayout.getTabAt(0).setIcon(R.drawable.tab_clothes);
        tabLayout.getTabAt(1).setIcon(R.drawable.tab_hygiene);
        tabLayout.getTabAt(2).setIcon(R.drawable.tab_health);
        tabLayout.getTabAt(3).setIcon(R.drawable.tab_documents);
        tabLayout.getTabAt(4).setIcon(R.drawable.tab_devices);
        tabLayout.getTabAt(5).setIcon(R.drawable.tab_other);
        tabLayout.getTabAt(6).setIcon(R.drawable.tab_food);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //Toast.makeText(getApplicationContext(), tab.getText() + "-" + tab.getPosition(), Toast.LENGTH_SHORT).show();
                type = tab.getText().toString();
                LoadData(extras.getString("tableName"), type, isPacked);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        if(isTemplate){
            findViewById(R.id.containerButtonsPacked).setVisibility(View.GONE);
            ImageView banner = findViewById(R.id.imageView);
            banner.setImageResource(R.drawable.template);
        }else{
            btnUnpacked = findViewById(R.id.btnUnpacked);
            btnPacked = findViewById(R.id.btnPacked);

            btnUnpacked.setOnClickListener(v -> {
                packed = 0;
                isPacked = false;
                setButtonActive();
                LoadData(extras.getString("tableName"), type, false);
            });

            btnPacked.setOnClickListener(v -> {
                packed = 1;
                isPacked = true;
                setButtonActive();
                LoadData(extras.getString("tableName"), type, true);
            });
            packed = 0;
        }
        type = "Clothes";
        LoadData(extras.getString("tableName"), "Clothes", false);

        ImageView btnAddItem = findViewById(R.id.btnAddItem);
        btnAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(FillList.this, Item.class);
            intent.putExtra("tableName", extras.getString("tableName"));
            intent.putExtra("fill", false);
            intent.putExtra("category", type);
            startActivityForResult(intent, 1);
        });

        ImageView btnPrevCat = findViewById(R.id.btnPrevCat);
        btnPrevCat.setOnClickListener(v->{
            if((tabLayout.getSelectedTabPosition() - 1) >= 0){
                TabLayout.Tab tab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition() - 1);
                tab.select();
            }
        });

        ImageView btnNextCat = findViewById(R.id.btnNextCat);
        btnNextCat.setOnClickListener(v->{
            if((tabLayout.getSelectedTabPosition() + 1) < tabLayout.getTabCount()){
                TabLayout.Tab tab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition() + 1);
                tab.select();
            }
        });
    }

    private void LoadData(String tableName, String type, boolean isPacked){
        DatabaseOpenHelper openHelper = new DatabaseOpenHelper(this);
        SQLiteDatabase database = openHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + tableName + " WHERE category=? AND packed=? ORDER BY name", new String[]{type, String.valueOf(packed)});

        ArrayList<SingleListAplikator> listsArray = new ArrayList<>();
        while(cursor.moveToNext()){
            listsArray.add(new SingleListAplikator(cursor.getString(1), cursor.getString(5), cursor.getString(4), cursor.getInt(6), cursor.getInt(2), cursor.getString(3)));
        }
        FillRecycler(listsArray, isPacked, tableName, isTemplate);
    }

    private void FillRecycler(final ArrayList<SingleListAplikator> array, boolean isPacked, String tName, boolean isTemplate){
        RecyclerView recyclerView = findViewById(R.id.listItems);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        if(isTemplate){
            AdapterPackedItems adapterLists = new AdapterPackedItems(array, replaceSpace(tName, false), true, false, null);
            adapterLists.setOnItemClickListener(position -> openItem(array.get(position)));
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapterLists);
            return;
        }

        if(isPacked){
            AdapterPackedItems adapterLists = new AdapterPackedItems(array, replaceSpace(tName, false), false, false, null);
            adapterLists.setOnItemClickListener(position -> openItem(array.get(position)));
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapterLists);
        }else {
            AdapterFillList adapterLists = new AdapterFillList(array, replaceSpace(tName, false), false, null);
            adapterLists.setOnItemClickListener(position -> openItem(array.get(position)));
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapterLists);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                if(!isTemplate){
                    if(packed == 0){
                        LoadData(extras.getString("tableName"), type, false);
                    }else {
                        LoadData(extras.getString("tableName"), type, true);
                    }
                    setButtonActive();
                }else {
                    LoadData(extras.getString("tableName"), type, false);
                }
                //System.out.println("UPDATE");
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(isTemplate){
            Intent main = new Intent(FillList.this, Templates.class);
            startActivity(main);
            finish();
        }else {
            Intent main = new Intent(FillList.this, Lists.class);
            startActivity(main);
            finish();
        }
    }

    public void openItem(SingleListAplikator item){
        Intent intent = new Intent(FillList.this, Item.class);
        intent.putExtra("fill", true);
        intent.putExtra("tableName", extras.getString("tableName"));

        intent.putExtra("name", item.getName());
        intent.putExtra("category", item.getCategory());
        intent.putExtra("packed", item.getPacked());
        intent.putExtra("pcs", item.getPcs());
        intent.putExtra("notes", item.getNotes());
        intent.putExtra("online", false);
        intent.putExtra("color", item.getColor());
        intent.putExtra("path", "");

        startActivityForResult(intent, 1);
    }
    private void setButtonActive(){
        if(packed == 1){
            btnPacked.setBackgroundResource(R.color.colorPrimaryDark);
            btnUnpacked.setBackgroundResource(R.color.white);
            btnUnpacked.setTextColor(Color.parseColor("#525252"));
            btnPacked.setTextColor(Color.parseColor("#FFFFFF"));
        }else {
            btnUnpacked.setBackgroundResource(R.color.colorPrimaryDark);
            btnPacked.setBackgroundResource(R.color.white);
            btnPacked.setTextColor(Color.parseColor("#525252"));
            btnUnpacked.setTextColor(Color.parseColor("#FFFFFF"));
        }
    }

    private String replaceSpace(String name, boolean replaceSpace) {
        if (!replaceSpace) {
            return name.replace("_", " ");
        }
        return name.replace(" ", "_");
    }
}
