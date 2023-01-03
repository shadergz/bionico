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

        TextView ipa_app_display_name;

        public IPAHolder(@NonNull View item_view) {
            super(item_view);
            ipa_app_display_name = item_view.findViewById(R.id.ipa_display_name);
        }
    }

    @NonNull
    @Override
    public IPAAdapter.IPAHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context main_context = parent.getContext();
        LayoutInflater main_inflater = LayoutInflater.from(main_context);
        View ipa_item_memory = main_inflater.inflate(R.layout.ipa_software_item, parent,
                false);

        return new IPAHolder(ipa_item_memory);
    }

    @Override
    public void onBindViewHolder(@NonNull IPAHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
