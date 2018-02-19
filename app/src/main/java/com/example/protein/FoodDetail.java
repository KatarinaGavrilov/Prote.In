package com.example.protein;

import android.content.Context;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.protein.Common.Common;
import com.example.protein.Databases.Database;
import com.example.protein.Model.Food;
import com.example.protein.Model.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FoodDetail extends AppCompatActivity {

    TextView food_name, food_price, food_description;
    ImageView food_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    CounterFab btnCart;
    ElegantNumberButton numberButton;

    String foodId="";

    FirebaseDatabase database;
    DatabaseReference foods;

    Food currentFood;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext( CalligraphyContextWrapper.wrap( newBase ) );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );

        // Note: add this code before setContentView method
        CalligraphyConfig.initDefault( new CalligraphyConfig.Builder()
                .setDefaultFontPath( "fonts/headlock.otf" )
                .setFontAttrId( R.attr.fontPath )
                .build());

        setContentView( R.layout.activity_food_detail );

        // Firebase
        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Food");

        // Init view
        numberButton = (ElegantNumberButton)findViewById( R.id.number_button );
        btnCart = (CounterFab) findViewById( R.id.btnCart );

        btnCart.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Database( getBaseContext() ).addToCart( new Order(
                        foodId,
                        currentFood.getName(),
                        numberButton.getNumber(),
                        currentFood.getPrice(),
                        currentFood.getDiscount(),
                        currentFood.getImage()
                ) );

                Toast.makeText( FoodDetail.this, "Dodato u korpu!", Toast.LENGTH_SHORT ).show();
            }
        } );

        btnCart.setCount( new Database( this ).getCountCart() );

        food_description = (TextView)findViewById( R.id.food_description );
        food_name = (TextView)findViewById( R.id.food_name );
        food_price = (TextView)findViewById( R.id.food_price );
        food_image = (ImageView)findViewById( R.id.img_food );

        collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById( R.id.collapsing );
        collapsingToolbarLayout.setExpandedTitleTextAppearance( R.style.ExpandedAppbar );
        collapsingToolbarLayout.setCollapsedTitleTextAppearance( R.style.CollapsedAppbar );

        // Get Food Id from Intent
        if (getIntent() != null)
            foodId = getIntent().getStringExtra( "FoodId" );
        if (!foodId.isEmpty()) {
            if (Common.isConnectedToInternet( getBaseContext() )) {
                getDetailFood(foodId);
            }
            else {
                Toast.makeText( FoodDetail.this, "Proverite internet konekciju!", Toast.LENGTH_SHORT ).show();
                return;
            }
        }
    }

    private void getDetailFood(String foodId){
        foods.child( foodId ).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentFood = dataSnapshot.getValue(Food.class);

                // Set Image
                Picasso.with( getBaseContext() ).load( currentFood.getImage() )
                        .into( food_image );

                collapsingToolbarLayout.setTitle( currentFood.getName() );

                food_price.setText(currentFood.getPrice() + " RSD" );

                food_name.setText( currentFood.getName() );

                food_description.setText( currentFood.getDescription() );
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        } );
    }
}
