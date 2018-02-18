package com.example.protein;

import android.content.DialogInterface;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.protein.Common.Common;
import com.example.protein.Databases.Database;
import com.example.protein.Model.Order;
import com.example.protein.Model.Request;
import com.example.protein.ViewHolder.CartAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Cart extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    TextView txtTotalPrice;
    Button btnPlace;

    List<Order> cart = new ArrayList<>(  );

    CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_cart );

        // Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        // Init
        recyclerView = (RecyclerView)findViewById( R.id.listCart );
        recyclerView.setHasFixedSize( true );
        layoutManager = new LinearLayoutManager( this );
        recyclerView.setLayoutManager( layoutManager );

        txtTotalPrice = (TextView)findViewById( R.id.total );
        btnPlace = (Button)findViewById( R.id.btnPlaceOrder );

        btnPlace.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (cart.size() > 0)
                    showAlertDialog();
                else
                    Toast.makeText( Cart.this, "Vaša korpa je prazna!", Toast.LENGTH_SHORT ).show();

            }
        } );

        loadListFood();

    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder( Cart.this );
        alertDialog.setTitle( "Još jedan korak!" );
        alertDialog.setMessage( "Ukucajte Vašu adresu:" );

        final EditText editAdress = new EditText( Cart.this );
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );

        editAdress.setLayoutParams( lp );
        alertDialog.setView( editAdress ); // Add edit Text to alert dialog
        alertDialog.setIcon( R.drawable.ic_shopping_cart_black_24dp );

        alertDialog.setPositiveButton( "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Create new Request
                Request request = new Request(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                        editAdress.getText().toString(),
                        txtTotalPrice.getText().toString(),
                        cart
                );

                // Submit to Firebase
                // We will using System.CurrentMilli to key
                requests.child( String.valueOf( System.currentTimeMillis() ) )
                        .setValue( request );

                // Delete cart
                new Database( getBaseContext() ).cleanCart();
                Toast.makeText( Cart.this, "Hvala!", Toast.LENGTH_SHORT ).show();
                finish();

            }
        } );

        alertDialog.setNegativeButton( "Odustani", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        } );

        alertDialog.show();

    }

    private void loadListFood() {
        cart = new Database(this).getCarts();
        adapter = new CartAdapter(cart, this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter( adapter );

        // Calculate total price
        Log.v( "PARS",  "" + cart );
        int total = 0;
        for (Order order:cart) {
            Log.v( "PARS",  "" + order );
            total+=(Integer.parseInt( order.getPrice() )) * (Integer.parseInt( order.getQuantity() ));
        }
        Locale locale = new Locale( "srp", "RS" );
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        txtTotalPrice.setText( fmt.format( total ) );
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals( Common.DELETE ))
            deleteCart(item.getOrder());
        return true;
    }

    private void deleteCart(int position) {
        // We will remove item at List<Order> by position
        cart.remove( position );

        // After that, we will delete all old data from SQLite
        new Database( this ).cleanCart();

        // And final, we will update new data from List<Order> to SQLite
        for (Order item:cart)
            new Database( this ).addToCart( item );

        // Refresh
        loadListFood();
    }

}
