package com.easierlifeapps.easypacking.Adapters;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.easierlifeapps.easypacking.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterTemplates extends RecyclerView.Adapter<AdapterTemplates.ListViewHolder>{
    private ArrayList<String> arrayList;
    private OnItemClickListener mListener;
    private boolean isOnline;

    FirebaseFirestore db;
    FirebaseAuth auth;
    FirebaseUser user;

    public interface OnItemClickListener {
        void onItemClick(int position);

    }
    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {
        public TextView txtName;
        public ImageButton btnShare;


        public ListViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            txtName = itemView.findViewById(R.id.itemName);
            btnShare = itemView.findViewById(R.id.btnShare);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }


    }
    public AdapterTemplates(ArrayList<String> exampleList, boolean isOnline){
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        arrayList = exampleList;
        this.isOnline = isOnline;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_template, parent, false);
        ListViewHolder lvh = new ListViewHolder(v, mListener);
        return lvh;
    }

    @Override
    public void onBindViewHolder(ListViewHolder listViewHolder, int i) {
        String currentItem = arrayList.get(i);
        listViewHolder.txtName.setText(currentItem);
        listViewHolder.btnShare.setOnClickListener(view->{
            System.out.println(arrayList.get(i));
            DatabaseOpenHelper openHelper = new DatabaseOpenHelper(listViewHolder.btnShare.getContext());
            SQLiteDatabase database = openHelper.getReadableDatabase();

            Map<String, Object> updatedList = new HashMap<>();
            updatedList.put("owner", user.getEmail());
            db.collection("templates").document(user.getEmail()).collection(arrayList.get(i)).document("table_info").set(updatedList);

            DocumentReference documentReference = db.collection("templates").document(user.getEmail());
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    List<String> array = new ArrayList<>();
                    if (documentSnapshot.exists()) {
                        for (String table : (ArrayList<String>) documentSnapshot.get("tables")) {
                            if(!table.equals(arrayList.get(i))){
                                array.add(table);
                            } else{
                                Toast.makeText(listViewHolder.btnShare.getContext(), "Change name of the template. Current name is already used.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        array.add(arrayList.get(i));
                        Map<String, Object> map = new HashMap<>();
                        map.put("tables", array);
                        db.collection("templates").document(user.getEmail()).set(map);
                    }else {
                        array.add(arrayList.get(i));
                        Map<String, Object> map = new HashMap<>();
                        map.put("tables", array);
                        db.collection("templates").document(user.getEmail()).set(map);
                    }
                    Toast.makeText(listViewHolder.btnShare.getContext(), "Template shared", Toast.LENGTH_SHORT).show();
                }
            });

            Cursor cursor = database.rawQuery("SELECT * FROM " + arrayList.get(i).replace(" ", "_"), null);
            while (cursor.moveToNext()){
                Map<String, Object> valuesItem = new HashMap<>();
                valuesItem.put("pcs", cursor.getInt(2));
                valuesItem.put("color", cursor.getString(3));
                valuesItem.put("category", cursor.getString(4));
                valuesItem.put("notes", cursor.getString(5));
                valuesItem.put("packed", 0);
                db.collection("templates").document(user.getEmail()).collection(arrayList.get(i)).document(cursor.getString(1)).set(valuesItem);
            }
        });
        if(isOnline){
            listViewHolder.btnShare.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }




}
