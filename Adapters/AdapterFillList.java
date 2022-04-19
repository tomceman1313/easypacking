package com.easierlifeapps.easypacking.Adapters;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

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

public class AdapterFillList extends RecyclerView.Adapter<AdapterFillList.ListViewHolder> {
    private ArrayList<SingleListAplikator> arrayList;
    private OnItemClickListener mListener;

    DatabaseOpenHelper openHelper;
    SQLiteDatabase database;
    String tableName, path;
    boolean online;
    FirebaseFirestore db;
    FirebaseAuth auth;
    FirebaseUser user;

    public interface OnItemClickListener {
        void onItemClick(int position);

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {
        public TextView txtName, txtPcs;
        public ImageButton btnPack;
        public Button personIndicator;


        public ListViewHolder(@NonNull final View itemView, final OnItemClickListener listener) {
            super(itemView);
            txtName = itemView.findViewById(R.id.itemName);
            txtPcs = itemView.findViewById(R.id.pcsNumber);
            btnPack = itemView.findViewById(R.id.btnPack);
            personIndicator = itemView.findViewById(R.id.personIndicator);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                    System.out.println("click" + txtName.getText());
                }
            });
        }


    }

    public AdapterFillList(ArrayList<SingleListAplikator> exampleList, String tableName, boolean online, String path) {
        arrayList = exampleList;
        this.tableName = tableName;
        this.online = online;
        this.path = path;

        if (online) {
            db = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();
            user = auth.getCurrentUser();
        }
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_fill_list, parent, false);
        ListViewHolder lvh = new ListViewHolder(v, mListener);
        return lvh;
    }

    @Override
    public void onBindViewHolder(ListViewHolder listViewHolder, final int i) {
        String currentItem = arrayList.get(i).getName();
        listViewHolder.txtName.setText(currentItem);
        listViewHolder.personIndicator.setBackgroundColor(Color.parseColor(arrayList.get(i).getColor()));
        final int pos = i;
        openHelper = new DatabaseOpenHelper(listViewHolder.btnPack.getContext());
        database = openHelper.getWritableDatabase();
        if (online) {
            DocumentReference documentReference;
            if(path == null){
                documentReference = db.collection("lists").document(user.getEmail()).collection(tableName).document("table_info");
            }else {
                documentReference = db.collection("lists").document(path).collection(tableName).document("table_info");
            }
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        int days = Integer.parseInt(documentSnapshot.get("days").toString());
                        String currentItemPcs = (arrayList.get(i).getPcs() == 0) ? days + " pcs" : arrayList.get(i).getPcs() + " pcs";
                        listViewHolder.txtPcs.setText(currentItemPcs);
                    }
                }
            });
        } else {
            Cursor cursor = database.rawQuery("SELECT * FROM table_lists WHERE table_name=?", new String[]{tableName});
            int days = 0;
            while (cursor.moveToNext()) {
                days = cursor.getInt(4);
            }
            String currentItemPcs = (arrayList.get(i).getPcs() == 0) ? days + " pcs" : arrayList.get(i).getPcs() + " pcs";
            listViewHolder.txtPcs.setText(currentItemPcs);
        }


        listViewHolder.btnPack.setOnClickListener(v -> {
            if (online) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                if(path == null){
                    db.collection("lists").document(user.getEmail()).collection(tableName).document(arrayList.get(pos).getName()).update("packed", 1);
                }else {
                    db.collection("lists").document(path).collection(tableName).document(arrayList.get(pos).getName()).update("packed", 1);
                }
            } else {
                ContentValues valuesItem = new ContentValues();
                valuesItem.put("packed", 1);
                database.update(tableName.replace(" ", "_"), valuesItem, "name = ?", new String[]{currentItem});
            }
            try{
                arrayList.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, arrayList.size());
            }catch (Exception e){
                System.out.println("--------------------------------------CRASH");
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }


}