package com.easierlifeapps.easypacking;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.easierlifeapps.easypacking.Adapters.AdapterFillList;
import com.easierlifeapps.easypacking.Adapters.AdapterPackedItems;
import com.easierlifeapps.easypacking.Adapters.SingleListAplikator;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class FillListOnline extends AppCompatActivity {

    TabLayout tabLayout;
    public int packed;
    Bundle extras;
    String type = "clothes";
    Button btnPacked, btnUnpacked;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser user;
    String externalPath;
    boolean isTemplate;

    /**
     *Aktivita sloužící pro práce uvitř online seznamu a šablony
     * Zobrazuje položky ve zvoleném listu a je možné jim měnit stav nebo v případě šablony je odmazávat
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_list);
        db = FirebaseFirestore.getInstance();
        extras = getIntent().getExtras();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user == null) {
            startActivityForResult(new Intent(FillListOnline.this, Login.class), 1);
        }
        externalPath = null;
        isTemplate = extras.getBoolean("template");

        TextView txtName = findViewById(R.id.txtListName);
        txtName.setText(extras.getString("tableName"));

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        ImageView btnEdit = findViewById(R.id.btnEditToolbar);
        btnEdit.setOnClickListener(v -> {
            Intent intent;
            if (isTemplate) {
                intent = new Intent(FillListOnline.this, TemplateInfo.class);
            } else {
                intent = new Intent(FillListOnline.this, ListInfo.class);
            }
            intent.putExtra("tableName", extras.getString("tableName"));
            intent.putExtra("online", true);
            intent.putExtra("path", externalPath);
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
                LoadData(extras.getString("tableName"), type, false);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        if (isTemplate) {
            findViewById(R.id.containerButtonsPacked).setVisibility(View.GONE);
            ImageView banner = findViewById(R.id.imageView);
            banner.setImageResource(R.drawable.template);
        } else {
            btnUnpacked = findViewById(R.id.btnUnpacked);
            btnPacked = findViewById(R.id.btnPacked);

            btnUnpacked.setOnClickListener(v -> {
                packed = 0;
                setButtonActive();
                LoadData(extras.getString("tableName"), type, false);
            });

            btnPacked.setOnClickListener(v -> {
                packed = 1;
                setButtonActive();
                LoadData(extras.getString("tableName"), type, true);
            });
            packed = 0;
        }
        type = "Clothes";
        LoadData(extras.getString("tableName"), "Clothes", false);
        ImageView btnAddItem = findViewById(R.id.btnAddItem);
        btnAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(FillListOnline.this, Item.class);
            intent.putExtra("tableName", extras.getString("tableName"));
            intent.putExtra("fill", false);
            intent.putExtra("online", true);
            intent.putExtra("template", isTemplate);
            intent.putExtra("category", type);
            if (externalPath != null) {
                intent.putExtra("path", externalPath);
            } else {
                intent.putExtra("path", "");
            }
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

    private void LoadData(String tableName, String type, boolean isPacked) {
        ArrayList<SingleListAplikator> listsArray = new ArrayList<>();

        DocumentReference tableInfo;
        if (isTemplate) {
            tableInfo = db.collection("templates").document(user.getEmail()).collection(tableName).document("table_info");
        } else {
            tableInfo = db.collection("lists").document(user.getEmail()).collection(tableName).document("table_info");
        }
        tableInfo.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                CollectionReference collection;

                if (isTemplate) {
                    collection = db.collection("templates").document(user.getEmail()).collection(tableName);
                } else {
                    if (documentSnapshot.get("path").equals("")) {
                        collection = db.collection("lists").document(user.getEmail()).collection(tableName);
                    } else {
                        collection = db.collection("lists").document(documentSnapshot.get("path").toString()).collection(tableName);
                        externalPath = documentSnapshot.get("path").toString();
                    }
                }


                collection.whereEqualTo("category", type).whereEqualTo("packed", packed).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot1 : queryDocumentSnapshots) {
                        if (!documentSnapshot1.getId().equals("table_info")) {
                            String name = documentSnapshot1.getId();
                            String color = documentSnapshot1.getString("color");
                            String notes = documentSnapshot1.getString("notes");
                            String type1 = documentSnapshot1.getString("category");
                            int packed = documentSnapshot1.getLong("packed").intValue();
                            int pcs = documentSnapshot1.getLong("pcs").intValue();
                            SingleListAplikator item = new SingleListAplikator(name, notes, type1, packed, pcs, color);
                            listsArray.add(item);
                        }
                    }
                    FillRecycler(listsArray, isPacked, tableName);
                });

            }
        });
    }

    private void FillRecycler(final ArrayList<SingleListAplikator> array, boolean isPacked, String tName) {
        RecyclerView recyclerView = findViewById(R.id.listItems);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        if (isTemplate) {
            AdapterPackedItems adapterLists = new AdapterPackedItems(array, tName, true, true, null);
            adapterLists.setOnItemClickListener(new AdapterPackedItems.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    openItem(array.get(position));
                }
            });
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapterLists);
            return;
        }

        if (isPacked) {
            AdapterPackedItems adapterLists = new AdapterPackedItems(array, tName, false, true, externalPath);
            adapterLists.setOnItemClickListener(new AdapterPackedItems.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    openItem(array.get(position));
                }
            });
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapterLists);
        } else {
            AdapterFillList adapterLists = new AdapterFillList(array, tName, true, externalPath);
            adapterLists.setOnItemClickListener(new AdapterFillList.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    openItem(array.get(position));
                }
            });
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapterLists);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (!isTemplate) {
                    if (packed == 0) {
                        LoadData(extras.getString("tableName"), type, false);
                    } else {
                        LoadData(extras.getString("tableName"), type, true);
                    }
                    setButtonActive();
                } else {
                    LoadData(extras.getString("tableName"), type, false);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent main;
        if (isTemplate) {
            main = new Intent(FillListOnline.this, Templates.class);
        } else {
            main = new Intent(FillListOnline.this, Lists.class);
        }
        startActivity(main);
        finish();
    }

    public void openItem(SingleListAplikator item) {
        Intent intent = new Intent(FillListOnline.this, Item.class);
        intent.putExtra("fill", true);
        intent.putExtra("tableName", extras.getString("tableName"));

        intent.putExtra("name", item.getName());
        intent.putExtra("category", item.getCategory());
        intent.putExtra("packed", item.getPacked());
        intent.putExtra("pcs", item.getPcs());
        intent.putExtra("notes", item.getNotes());
        intent.putExtra("online", true);
        intent.putExtra("color", item.getColor());
        intent.putExtra("template", isTemplate);
        if (externalPath != null) {
            intent.putExtra("path", externalPath);
        } else {
            intent.putExtra("path", "");
        }

        startActivityForResult(intent, 1);
    }

    private void setButtonActive() {
        if (packed == 1) {
            btnPacked.setBackgroundResource(R.color.colorPrimaryDark);
            btnUnpacked.setBackgroundResource(R.color.white);
            btnUnpacked.setTextColor(Color.parseColor("#525252"));
            btnPacked.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            btnUnpacked.setBackgroundResource(R.color.colorPrimaryDark);
            btnPacked.setBackgroundResource(R.color.white);
            btnPacked.setTextColor(Color.parseColor("#525252"));
            btnUnpacked.setTextColor(Color.parseColor("#FFFFFF"));
        }
    }
}
