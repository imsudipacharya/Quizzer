package com.bma.quizzer;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.viewHolder> {
private final List<CategoryModel> categoryModelList;

    public CategoryAdapter(List<CategoryModel> categoryModelList) {
        this.categoryModelList = categoryModelList;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
holder.setData(categoryModelList.get(position).getUrl(),categoryModelList.get(position).getName(), categoryModelList.get(position).getSets());
    }

    @Override
    public int getItemCount() {
        return categoryModelList.size();
    }

    class viewHolder extends RecyclerView.ViewHolder{

        private ImageView imageViews;
        private TextView titles;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            imageViews = itemView.findViewById(R.id.imageViews);
            titles = itemView.findViewById(R.id.titles);
        }

        private void setData(String url, String title,final int sets){
            Glide.with(itemView.getContext()).load(url).into(imageViews);
            this.titles.setText(title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(itemView.getContext(),SetsActivity.class);
                    intent.putExtra("title", title);
                    intent.putExtra("sets", sets);
                    itemView.getContext().startActivity(intent);
                }
            });
        }
    }
}
