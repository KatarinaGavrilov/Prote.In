package com.example.protein.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.protein.Interface.ItemClickListener;
import com.example.protein.R;

/**
 * Created by gavri on 15/02/2018.
 */

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView food_name, food_price;
    public ImageView food_image, quick_cart;
    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public FoodViewHolder(View itemView) {
        super( itemView );

        food_name = (TextView)itemView.findViewById( R.id.food_name );
        food_image = (ImageView)itemView.findViewById( R.id.food_image );
        food_price = (TextView) itemView.findViewById( R.id.food_price );
        quick_cart = (ImageView)itemView.findViewById( R.id.btn_quick_cart );

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick( view, getAdapterPosition(), false );
    }
}
