package com.easierlifeapps.easypacking.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.easierlifeapps.easypacking.Objects.Journey;
import com.easierlifeapps.easypacking.R;

import java.util.ArrayList;

public class AdapterJournal extends RecyclerView.Adapter<AdapterJournal.ListViewHolder>{
    private ArrayList<Journey> arrayList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);

    }
    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {
        public TextView txtName;
        public TextView txtDate;


        public ListViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            txtName = itemView.findViewById(R.id.itemName);
            txtDate = itemView.findViewById(R.id.date);
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
    public AdapterJournal(ArrayList<Journey> exampleList){
        arrayList = exampleList;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_journal, parent, false);
        ListViewHolder lvh = new ListViewHolder(v, mListener);
        return lvh;
    }

    @Override
    public void onBindViewHolder(ListViewHolder listViewHolder, int i) {
        listViewHolder.txtName.setText(arrayList.get(i).getName());
        listViewHolder.txtDate.setText(arrayList.get(i).getDate());
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }




}
