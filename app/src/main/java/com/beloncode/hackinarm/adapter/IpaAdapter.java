package com.beloncode.hackinarm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beloncode.hackinarm.IpaObject;
import com.beloncode.hackinarm.R;

import java.util.Vector;

class IpaPresentation {
    IpaPresentation(final IpaObject ipaObject) {
        packageName = ipaObject.ipaFilename;
    }

    public String packageName;
}

public class IpaAdapter extends RecyclerView.Adapter<IpaAdapter.IpaHolder> {

    private final Vector<IpaPresentation> ipaCollection;

    private IpaPresentation getPresentationFromIpa(final IpaObject ipaItem) {
        for (IpaPresentation presentation : ipaCollection) {
            if (!presentation.packageName.equals(ipaItem.ipaFilename)) continue;
            return presentation;
        }
        return null;
    }

    private int getPresentationIndex(final IpaObject ipaObject) {
        return ipaCollection.indexOf(getPresentationFromIpa(ipaObject));
    }

    public IpaAdapter() {
        ipaCollection = new Vector<>();
    }

    public void placeNewItem(final IpaObject ipaItem) {
        final IpaPresentation ipaPresentObject = new IpaPresentation(ipaItem);
        ipaCollection.add(ipaPresentObject);
        // Once added, we can search through the presentation vector and find the exactly
        // position of our object!
        notifyItemChanged(getPresentationIndex(ipaItem));
    }

    static public class IpaHolder extends RecyclerView.ViewHolder {

        TextView ipaAppDspName;

        public IpaHolder(@NonNull View itemView) {
            super(itemView);
            ipaAppDspName = itemView.findViewById(R.id.ipa_display_name);
        }
    }

    @NonNull
    @Override
    public IpaHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context mainContext = parent.getContext();
        LayoutInflater mainInflater = LayoutInflater.from(mainContext);
        View ipaItemMem = mainInflater.inflate(R.layout.ipa_software_item, parent, false);

        return new IpaHolder(ipaItemMem);
    }

    @Override
    public void onBindViewHolder(@NonNull IpaHolder holder, int position) {
        final IpaPresentation generateIpa = ipaCollection.get(position);
        TextView ipaObjectText = holder.ipaAppDspName;
        ipaObjectText.setText(generateIpa.packageName);
    }

    @Override
    public int getItemCount() {
        return ipaCollection.size();
    }
}
