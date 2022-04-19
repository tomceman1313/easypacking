package com.easierlifeapps.easypacking;

import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.Toast;

import com.easierlifeapps.easypacking.Adapters.DatabaseOpenHelper;

public class Post extends AppCompatActivity {
    DatabaseOpenHelper openHelper;
    SQLiteDatabase database;
    Bundle extras;
    EditText title;
    EditText post;

    /**
     * Aktivita sloužící k vložení nového přispěvku nebo zobrazení detailu zvoleného
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        openHelper = new DatabaseOpenHelper(this);
        database = openHelper.getReadableDatabase();
        extras = getIntent().getExtras();

        title = findViewById(R.id.inputTitle);
        post = findViewById(R.id.inputPost);

        Button save = findViewById(R.id.btnSavePost);
        save.setOnClickListener(v -> {
            String titleCont = title.getText().toString();
            String postCont = post.getText().toString();

            if (TextUtils.isEmpty(titleCont)) {
                title.setError("Title cannot be empty");
                title.requestFocus();
            } else if (TextUtils.isEmpty(postCont)) {
                post.setError("Post cannot be empty");
                post.requestFocus();
            } else {
                if(!uniqueCheck(titleCont)){
                    return;
                }
                ContentValues values = new ContentValues();
                values.put("title", titleCont);
                values.put("content", postCont);

                if(extras.getString("post") != null){
                    database.update(extras.getString("tableName"), values, "title = ?", new String[]{extras.getString("post")});
                }else {
                    database.insert(extras.getString("tableName"), null, values);
                }
                onBackPressed();
            }
        });

        if(extras.getString("post") != null){
            setContentPost();
            findViewById(R.id.btnDelete).setOnClickListener(v->{
                DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            database.delete(extras.getString("tableName"), "title = ?", new String[]{extras.getString("post")});
                            System.out.println("Post deleted");
                            database.close();
                            onBackPressed();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(Post.this);
                builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            });
        }else{
            findViewById(R.id.btnDelete).setVisibility(View.GONE);
        }
    }
    //Metoda nastaví obsah polí podle zvoleného příspěvku
    private void setContentPost(){
        title.setText(extras.getString("post"));

        Cursor cursor = database.rawQuery("SELECT * FROM " + extras.getString("tableName") +" WHERE title = ?", new String[]{extras.getString("post")});
        while (cursor.moveToNext()){
            post.setText(cursor.getString(2));
        }

    }

    private boolean uniqueCheck(String name){
        String tableName = extras.getString("tableName");
        if(tableName.equals(name)){
            return true;
        }

        Cursor cursor = database.rawQuery("SELECT * FROM " + extras.getString("tableName") +" WHERE title = ?", new String[]{name});

        if(cursor.getCount() > 0){
            Toast.makeText(this, "Title is already used", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Post.this, JourneyDetail.class);
        intent.putExtra("tableName", extras.getString("tableName"));
        startActivity(intent);
        finish();
    }
}
