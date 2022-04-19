package com.easierlifeapps.easypacking.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.easierlifeapps.easypacking.R;

import java.util.ArrayList;

public class AdapterLists extends RecyclerView.Adapter<AdapterLists.ListViewHolder>{
    private ArrayList<ListsAplikator> arrayList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);

    }
    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {
        public TextView txtName;
        public ProgressBar progressBar;
        public TextView txtPercentage;


        public ListViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            txtName = itemView.findViewById(R.id.itemName);
            progressBar = itemView.findViewById(R.id.progressBar);
            txtPercentage = itemView.findViewById(R.id.percentage);
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
    public AdapterLists(ArrayList<ListsAplikator> exampleList){
        arrayList = exampleList;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_list, parent, false);
        ListViewHolder lvh = new ListViewHolder(v, mListener);
        return lvh;
    }

    @Override
    public void onBindViewHolder(ListViewHolder listViewHolder, int i) {
        listViewHolder.progressBar.setMax(100);
        listViewHolder.progressBar.setProgress(arrayList.get(i).getPercentage());
        listViewHolder.txtPercentage.setText(arrayList.get(i).getPercentage() + "%");
        listViewHolder.txtName.setText(arrayList.get(i).getName());
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }




}
