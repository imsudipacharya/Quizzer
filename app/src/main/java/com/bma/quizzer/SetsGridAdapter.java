package com.bma.quizzer;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SetsGridAdapter extends BaseAdapter {
    private int sets = 0;
    private String category;

    public SetsGridAdapter(int sets, String category) {
        this.sets = sets;
        this.category = category;
    }

    @Override
    public int getCount() {
        return sets;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view ;
        if(convertView == null){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.set_item,parent,false);
        }else{
            view = convertView;
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(parent.getContext(), QuestionsActivity.class);
                intent.putExtra("category",category);
                intent.putExtra("setNo", position+1);
                view.getContext().startActivity(intent);
            }
        });

        ((TextView)view.findViewById(R.id.textViews)).setText(String.valueOf(position+1));
        return view;
    }
}
