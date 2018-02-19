package com.example.protein;

import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.protein.Common.Common;
import com.example.protein.Databases.Database;
import com.example.protein.Interface.ItemClickListener;
import com.example.protein.Model.Food;
import com.example.protein.Model.Order;
import com.example.protein.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference foodList;
    String categoryId="";
    FirebaseRecyclerAdapter<Food,FoodViewHolder> adapter;

    // Search Functionality
    FirebaseRecyclerAdapter<Food,FoodViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>(  );
    MaterialSearchBar materialSearchBar;

    SwipeRefreshLayout swipeRefreshLayout;

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

        setContentView( R.layout.activity_food_list );

        //Firebase
        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Food");

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById( R.id.swipe_layout );
        swipeRefreshLayout.setColorSchemeResources( R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Get Intent here
                if (getIntent() != null)
                    categoryId = getIntent().getStringExtra( "CategoryId" );
                if (!categoryId.isEmpty() && categoryId != null) {
                    if (Common.isConnectedToInternet( getBaseContext() )) {
                        loadListFood(categoryId);
                    }
                    else {
                        Toast.makeText( FoodList.this, "Proverite internet konekciju!", Toast.LENGTH_SHORT ).show();
                        return;
                    }
                }
            }
        } );
        swipeRefreshLayout.post( new Runnable() {
            @Override
            public void run() {
                // Get Intent here
                if (getIntent() != null)
                    categoryId = getIntent().getStringExtra( "CategoryId" );
                if (!categoryId.isEmpty() && categoryId != null) {
                    if (Common.isConnectedToInternet( getBaseContext() )) {
                        loadListFood(categoryId);
                    }
                    else {
                        Toast.makeText( FoodList.this, "Proverite internet konekciju!", Toast.LENGTH_SHORT ).show();
                        return;
                    }
                }
            }
        } );

        recyclerView = (RecyclerView)findViewById( R.id.recycler_food );
        recyclerView.setHasFixedSize( true );
        layoutManager = new LinearLayoutManager( this );
        recyclerView.setLayoutManager( layoutManager );

        //Search
        materialSearchBar = (MaterialSearchBar)findViewById( R.id.searchBar );
        materialSearchBar.setHint( "Pretraga" );
        // materialSearchBar.setSpeechMode( false ); No need, because we already define it at XML
        loadSuggest(); // Write function to load Suggest from Firebase
        // materialSearchBar.setLastSuggestions( suggestList );
        materialSearchBar.setCardViewElevation( 10 );
        materialSearchBar.addTextChangeListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // When user type their text, we will change suggest list

                List<String> suggest = new ArrayList<String>(  );
                for (String search:suggestList) {
                    if (search.toLowerCase().contains( materialSearchBar.getText().toLowerCase() ))
                        suggest.add( search );
                }
                materialSearchBar.setLastSuggestions( suggest );
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        } );
        materialSearchBar.setOnSearchActionListener( new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                // When Search Bar is close
                // Restore original suggest adapter
                if (!enabled)
                    recyclerView.setAdapter( adapter );
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                // When search finish
                // Show result of search adapter
                startSearch( text );

            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        } );

    }

    private void startSearch(CharSequence text) {
        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodList.orderByChild( "Name" ).equalTo( text.toString() )
        ) {
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {
                viewHolder.food_name.setText( model.getName() );
                Picasso.with( getBaseContext() ).load( model.getImage() )
                        .into( viewHolder.food_image );

                final Food local = model;
                viewHolder.setItemClickListener( new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        // Start new Activity
                        Intent foodDetail = new Intent(FoodList.this, FoodDetail.class);
                        foodDetail.putExtra( "FoodId", searchAdapter.getRef( position ).getKey() ); // Send Food Id to new activity
                        startActivity( foodDetail );
                    }
                } );
            }

            @Override
            public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from( parent.getContext() )
                        .inflate( R.layout.food_item, parent, false );
                return new FoodViewHolder( itemView );
            }
        };
        recyclerView.setAdapter( searchAdapter ); // Set adapter for Recylcer View is Search result

    }

    private void loadSuggest() {
        foodList.orderByChild( "MenuId").equalTo( categoryId )
                .addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot:dataSnapshot.getChildren()) {
                            Food item = postSnapshot.getValue(Food.class);
                            suggestList.add( item.getName() ); // Add name of food to suggest list
                        }
                        materialSearchBar.setLastSuggestions( suggestList );
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                } );
    }

    private void loadListFood(String categoryId) {
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodList.orderByChild( "MenuId" ).equalTo( categoryId )
            ) {
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, final Food model, final int position) {
                viewHolder.food_name.setText( model.getName() );
                viewHolder.food_price.setText( String.format( "%s RSD", model.getPrice().toString() ) );
                Picasso.with( getBaseContext() ).load( model.getImage() )
                        .into( viewHolder.food_image );

                // Quick cart
                viewHolder.quick_cart.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new Database( getBaseContext() ).addToCart( new Order(
                                adapter.getRef( position ).getKey(),
                                model.getName(),
                                "1",
                                model.getPrice(),
                                model.getDiscount(),
                                model.getImage()
                        ) );

                        Toast.makeText( FoodList.this, "Dodato u korpu!", Toast.LENGTH_SHORT ).show();
                    }
                } );

                final Food local = model;
                viewHolder.setItemClickListener( new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        // Start new Activity
                        Intent foodDetail = new Intent(FoodList.this, FoodDetail.class);
                        foodDetail.putExtra( "FoodId", adapter.getRef( position ).getKey() ); // Send Food Id to new activity
                        startActivity( foodDetail );

                    }
                } );
            }

            @Override
            public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from( parent.getContext() )
                        .inflate( R.layout.food_item, parent, false );
                return new FoodViewHolder( itemView );
            }
        };
        // Set Adapter
        recyclerView.setAdapter( adapter );
        swipeRefreshLayout.setRefreshing( false );
    }


}
