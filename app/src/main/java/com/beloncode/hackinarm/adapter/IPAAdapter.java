package com.beloncode.hackinarm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beloncode.hackinarm.R;

public class IPAAdapter extends RecyclerView.Adapter<IPAAdapter.IPAHolder> {

    static public class IPAHolder extends RecyclerView.ViewHolder {

        TextView ipaAppDisplayName;

        public IPAHolder(@NonNull View itemView) {
            super(itemView);
            ipaAppDisplayName = itemView.findViewById(R.id.ipa_display_name);
        }
    }

    @NonNull
    @Override
    public IPAAdapter.IPAHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context mainContext = parent.getContext();
        LayoutInflater mainInflater = LayoutInflater.from(mainContext);
        View ipaItemInMemory = mainInflater.inflate(R.layout.ipa_software_item, parent, false);

        return new IPAHolder(ipaItemInMemory);
    }

    @Override
    public void onBindViewHolder(@NonNull IPAHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
