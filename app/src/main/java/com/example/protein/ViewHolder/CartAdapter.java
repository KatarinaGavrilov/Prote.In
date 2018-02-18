package com.example.protein.ViewHolder;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.example.protein.Cart;
import com.example.protein.Common.Common;
import com.example.protein.Interface.ItemClickListener;
import com.example.protein.Model.Order;
import com.example.protein.R;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by gavri on 16/02/2018.
 */

class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    , View.OnCreateContextMenuListener {

    public TextView txt_cart_name, txt_price;
    public ImageView img_cart_count;
    public ImageView cart_image;

    private ItemClickListener itemClickListener;

    public void setTxt_cart_name(TextView txt_cart_name) {
        this.txt_cart_name = txt_cart_name;
    }

    public CartViewHolder(View itemView) {
        super( itemView );

        txt_cart_name = (TextView)itemView.findViewById( R.id.cart_item_name );
        txt_price = (TextView)itemView.findViewById( R.id.cart_item_Price );
        img_cart_count = (ImageView)itemView.findViewById( R.id.cart_item_count );
        cart_image = (ImageView)itemView.findViewById( R.id.cart_image );

        itemView.setOnCreateContextMenuListener( this );
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle( "Izaberite akciju" );
        contextMenu.add( 0,0,getAdapterPosition(), Common.DELETE );
    }
}

public class CartAdapter extends RecyclerView.Adapter<CartViewHolder> {

    private List<Order> listData = new ArrayList<>(  );
    private Cart cart;

    public CartAdapter(List<Order> listData, Cart cart) {
        this.listData = listData;
        this.cart = cart;
    }

    @Override
    public CartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from( cart );
        View itemView = inflater.inflate( R.layout.cart_layout, parent, false );
        return new CartViewHolder( itemView );
    }

    @Override
    public void onBindViewHolder(CartViewHolder holder, int position) {

        Picasso.with( cart.getBaseContext() )
                .load( listData.get( position ).getImage() )
                .resize( 70,70 )
                .centerCrop()
                .into( holder.cart_image );

        TextDrawable drawable = TextDrawable.builder()
                .buildRound( ""+listData.get( position ).getQuantity(), Color.RED );
        holder.img_cart_count.setImageDrawable( drawable );

        Locale locale = new Locale( "srp", "RS" );
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        int price = (Integer.parseInt( listData.get( position ).getPrice() )) * (Integer.parseInt( listData.get( position ).getQuantity() ));
        holder.txt_price.setText( fmt.format( price ) );
        holder.txt_cart_name.setText( listData.get( position ).getProductName() );
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }
}
