package com.example.protein;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.example.protein.Common.Common;
import com.example.protein.Databases.Database;
import com.example.protein.Interface.ItemClickListener;
import com.example.protein.Model.Category;
import com.example.protein.Model.Order;
import com.example.protein.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseDatabase database;
    DatabaseReference category;
    TextView txtFullName;
    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;

    SwipeRefreshLayout swipeRefreshLayout;

    CounterFab fab;

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

        setContentView( R.layout.activity_home );


        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
        toolbar.setTitle( "Meni" );
        setSupportActionBar( toolbar );

        // View
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById( R.id.swipe_layout );
        swipeRefreshLayout.setColorSchemeResources( R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Common.isConnectedToInternet( getBaseContext() )) {
                    loadMenu();
                }
                else {
                    Toast.makeText( getBaseContext(), "Proverite internet konekciju!", Toast.LENGTH_SHORT ).show();
                    return;
                }
            }
        } );

        // Default, load for first time
        swipeRefreshLayout.post( new Runnable() {
            @Override
            public void run() {
                if (Common.isConnectedToInternet( getBaseContext() )) {
                    loadMenu();
                }
                else {
                    Toast.makeText( getBaseContext(), "Proverite internet konekciju!", Toast.LENGTH_SHORT ).show();
                    return;
                }
            }
        } );

        //Init Firebase
        database = FirebaseDatabase.getInstance();
        category = database.getReference("Category");

        Paper.init( this );

        fab = (CounterFab) findViewById( R.id.fab );
        fab.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cartIntent = new Intent( Home.this, Cart.class );
                startActivity( cartIntent );
            }
        } );

        fab.setCount( new Database(this).getCountCart() );

        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout );
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close );
        drawer.addDrawerListener( toggle );
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById( R.id.nav_view );
        navigationView.setNavigationItemSelectedListener( this );


        //Set Name for user
        View headerView = navigationView.getHeaderView( 0 );
        txtFullName = (TextView)headerView.findViewById( R.id.txtFullName );
        txtFullName.setText( Common.currentUser.getName() );

        //Load menu
        recycler_menu = (RecyclerView)findViewById( R.id.recycler_menu );
        recycler_menu.setHasFixedSize( true );
        layoutManager = new LinearLayoutManager( this );
        recycler_menu.setLayoutManager( layoutManager );

    }

    @Override
    protected void onResume() {
        super.onResume();
        fab.setCount( new Database(this).getCountCart() );

    }

    private void loadMenu() {
       adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(Category.class, R.layout.menu_item, MenuViewHolder.class, category) {
           @Override
           protected void populateViewHolder(MenuViewHolder viewHolder, Category model, int position) {
               viewHolder.txtMenuName.setText( model.getName() );
               Picasso.with( getBaseContext() ).load( model.getImage() )
                       .into( viewHolder.imageView );
               final Category clickItem = model;
               viewHolder.setItemClickListener( new ItemClickListener() {
                   @Override
                   public void onClick(View view, int position, boolean isLongClick) {
                       // Get CategoryId and send to new Activity
                       Intent foodList = new Intent( Home.this, FoodList.class );
                       // Because CategoryId is key, so we just get key of this item
                       foodList.putExtra( "CategoryId", adapter.getRef( position ).getKey() );
                       startActivity( foodList );
                   }
               } );
           }
       };
       recycler_menu.setAdapter( adapter );
       swipeRefreshLayout.setRefreshing( false );
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout );
        if (drawer.isDrawerOpen( GravityCompat.START )) {
            drawer.closeDrawer( GravityCompat.START );
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.home, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_search)
            startActivity( new Intent( Home.this, SearchActivity.class ) );

        return super.onOptionsItemSelected( item );
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {

        } else if (id == R.id.nav_cart) {
            Intent cartIntent = new Intent( Home.this, Cart.class );
            startActivity( cartIntent );

        } else if (id == R.id.nav_orders) {
            Intent orderIntent = new Intent( Home.this, OrderStatus.class );
            startActivity( orderIntent );

        } else if (id == R.id.nav_log_out) {
            // Delete Remember user & password
            Paper.book().destroy();

            // Logout
            Intent signIn = new Intent( Home.this, SignIn.class );
            signIn.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK );
            startActivity( signIn );

        } else if (id == R.id.nav_change_pwd) {
            showChangePasswordDialog();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById( R.id.drawer_layout );
        drawer.closeDrawer( GravityCompat.START );
        return true;
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder( Home.this );
        alertDialog.setTitle( "PROMENA LOZINKE" );
        alertDialog.setMessage( "Unesite sledeće podatke" );

        LayoutInflater inflater = LayoutInflater.from( this );
        View layout_pwd = inflater.inflate( R.layout.change_password_layout, null );

        final MaterialEditText editPassword = (MaterialEditText)layout_pwd.findViewById( R.id.editPassword );
        final MaterialEditText editNewPassword = (MaterialEditText)layout_pwd.findViewById( R.id.editNewPassword );
        final MaterialEditText editRepeatPassword = (MaterialEditText)layout_pwd.findViewById( R.id.editRepeatPassword );

        alertDialog.setView( layout_pwd );

        // Button
        alertDialog.setPositiveButton( "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Change password here

                // For use SpotsDialog, please use AlertDialog from android.app, not from v7 like above AlertDialog
                final android.app.AlertDialog waitingDialog = new SpotsDialog( Home.this );
                waitingDialog.show();

                // Check old password
                if (editPassword.getText().toString().equals( Common.currentUser.getPassword() )) {
                    // Check new password and repeat password
                    if (editNewPassword.getText().toString().equals( editRepeatPassword.getText().toString() )) {
                        Map<String, Object> passwordUpdate = new HashMap<>(  );
                        passwordUpdate.put( "Password", editNewPassword.getText().toString() );

                        // Make update
                        DatabaseReference user = FirebaseDatabase.getInstance().getReference("User");
                        user.child( Common.currentUser.getPhone() )
                                .updateChildren( passwordUpdate )
                                .addOnCompleteListener( new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        waitingDialog.dismiss();
                                        Toast.makeText( Home.this, "Lozinka je promenjena!", Toast.LENGTH_SHORT ).show();
                                    }
                                } )
                                .addOnFailureListener( new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText( Home.this, e.getMessage(), Toast.LENGTH_SHORT ).show();
                                    }
                                } );
                    }
                    else {
                        waitingDialog.dismiss();
                        Toast.makeText( Home.this, "Lozinke nisu iste!", Toast.LENGTH_SHORT ).show();
                    }
                }
                else {
                    waitingDialog.dismiss();
                    Toast.makeText( Home.this, "Pogrešna lozinka!", Toast.LENGTH_SHORT ).show();
                }
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
}
