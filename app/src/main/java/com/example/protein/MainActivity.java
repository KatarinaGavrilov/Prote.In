package com.example.protein;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.protein.Common.Common;
import com.example.protein.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    Button btnSignIn, btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        btnSignIn = (Button)findViewById( R.id.btnSignIn );
        btnSignUp = (Button)findViewById( R.id.btnSignUp );

        // Init Paper
        Paper.init(this);

        btnSignIn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signIn = new Intent( MainActivity.this, SignIn.class );
                startActivity( signIn );
            }
        } );

        btnSignUp.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signUp = new Intent( MainActivity.this, SignUp.class );
                startActivity( signUp );
            }
        } );

        // Check remember
        String user = Paper.book().read( Common.USER_KEY );
        String pwd = Paper.book().read( Common.PWD_KEY );
        if (user != null && pwd != null) {
            if (!user.isEmpty() && !pwd.isEmpty()) {
                login(user,pwd);
            }
        }

    }

    private void login(final String phone, final String pwd) {
        // Just copy login code from SignIn.claaa

        //Init Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        if (Common.isConnectedToInternet( getBaseContext() )) {

            final ProgressDialog mDialog = new ProgressDialog( MainActivity.this );
            mDialog.setMessage( "Molimo sačekajte..." );
            mDialog.show();

            table_user.addValueEventListener( new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    //Check if user not exist in database
                    if (dataSnapshot.child( phone ).exists()) {
                        //Get User information
                        mDialog.dismiss();
                        User user = dataSnapshot.child( phone ).getValue( User.class );
                        user.setPhone( phone ); // set Phone
                        if (user.getPassword().equals( pwd )) {
                            Toast.makeText( MainActivity.this, "Uspešno logovanje!", Toast.LENGTH_SHORT ).show();
                            Intent homeIntent = new Intent( MainActivity.this, Home.class );
                            Common.currentUser = user;
                            startActivity( homeIntent );
                            finish();
                        } else {
                            Toast.makeText( MainActivity.this, "Pogrešna lozinka!!!", Toast.LENGTH_SHORT ).show();
                        }
                    } else {
                        mDialog.dismiss();
                        Toast.makeText( MainActivity.this, "Korisnik ne postoji!", Toast.LENGTH_SHORT ).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            } );
        }
        else {
            Toast.makeText( MainActivity.this, "Proverite internet konekciju!", Toast.LENGTH_SHORT ).show();
            return;
        }
    }
}
