package com.example.protein;

import android.content.Intent;
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

public class SearchActivity extends AppCompatActivity {

    FirebaseRecyclerAdapter<Food,FoodViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>(  );
    MaterialSearchBar materialSearchBar;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference foodList;

    FirebaseRecyclerAdapter<Food,FoodViewHolder> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_search );

        //Firebase
        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Food");

        recyclerView = (RecyclerView)findViewById( R.id.recycler_search );
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

        // Load all food
        loadAllFoods();

    }

    private void loadAllFoods() {
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodList
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

                        Toast.makeText( SearchActivity.this, "Dodato u korpu!", Toast.LENGTH_SHORT ).show();
                    }
                } );

                final Food local = model;
                viewHolder.setItemClickListener( new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        // Start new Activity
                        Intent foodDetail = new Intent(SearchActivity.this, FoodDetail.class);
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
                        Intent foodDetail = new Intent(SearchActivity.this, FoodDetail.class);
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
        foodList.addListenerForSingleValueEvent( new ValueEventListener() {
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



}
