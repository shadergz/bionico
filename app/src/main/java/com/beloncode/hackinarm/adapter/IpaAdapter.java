package com.beloncode.hackinarm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beloncode.hackinarm.IpaObjectFront;
import com.beloncode.hackinarm.R;

import java.util.Vector;

class IpaPresentation {
    IpaPresentation(final IpaObjectFront ipa_object) {
        m_ipa_package_name = ipa_object.m_ipa_filename;
    }
    public String m_ipa_package_name;
}

public class IpaAdapter extends RecyclerView.Adapter<IpaAdapter.IPAHolder> {

    private final Vector<IpaPresentation> m_ipa_collection;

    private IpaPresentation getPresentationFromIPA(final IpaObjectFront ipa_item) {
        for (IpaPresentation presentation : m_ipa_collection) {
            if (!presentation.m_ipa_package_name.equals(ipa_item.m_ipa_filename)) continue;
            return presentation;
        }
        return null;
    }

    private int getPresentationIndex(final IpaObjectFront ipa_object) {
        return m_ipa_collection.indexOf(getPresentationFromIPA(ipa_object));
    }

    public IpaAdapter() {
        m_ipa_collection = new Vector<>();
    }

    public void placeNewItem(final IpaObjectFront ipa_item) {
        final IpaPresentation ipa_present_object = new IpaPresentation(ipa_item);
        m_ipa_collection.add(ipa_present_object);
        // Once added, we can search through the presentation vector and find the exactly
        // position of our object!
        notifyItemChanged(getPresentationIndex(ipa_item));
    }

    static public class IPAHolder extends RecyclerView.ViewHolder {

        TextView ipa_app_display_name;

        public IPAHolder(@NonNull View item_view) {
            super(item_view);
            ipa_app_display_name = item_view.findViewById(R.id.ipa_display_name);
        }
    }

    @NonNull
    @Override
    public IpaAdapter.IPAHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context main_context = parent.getContext();
        LayoutInflater main_inflater = LayoutInflater.from(main_context);
        View ipa_item_memory = main_inflater.inflate(R.layout.ipa_software_item, parent,
                false);

        return new IPAHolder(ipa_item_memory);
    }

    @Override
    public void onBindViewHolder(@NonNull IPAHolder holder, int position) {
        final IpaPresentation generate_ipa = m_ipa_collection.get(position);
        TextView ipa_object_text = holder.ipa_app_display_name;
        ipa_object_text.setText(generate_ipa.m_ipa_package_name);
    }

    @Override
    public int getItemCount() {
        return m_ipa_collection.size();
    }
}
