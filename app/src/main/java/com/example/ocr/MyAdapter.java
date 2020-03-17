package com.example.ocr;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MyAdapter extends ArrayAdapter<HistoryItem> {

    private int resourceId;

    public MyAdapter(Context context, int textViewResourceId, List<HistoryItem> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HistoryItem item = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        ImageView image = view.findViewById(R.id.image);
        TextView text = view.findViewById(R.id.text);
        image.setImageBitmap(item.getImage());
        text.setText(item.getText());
        return view;
    }
}
