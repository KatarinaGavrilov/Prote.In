package com.example.protein;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.protein.Common.Common;
import com.example.protein.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.CheckBox;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignIn extends AppCompatActivity {

    EditText editPhone, editPassword;
    Button btnSignIn;
    CheckBox ckbRemember;
    TextView txtForgotPwd;

    FirebaseDatabase database;
    DatabaseReference table_user;

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

        setContentView( R.layout.activity_sign_in );

        editPhone = (MaterialEditText)findViewById( R.id.editPhone );
        editPassword = (MaterialEditText)findViewById( R.id.editPassword );
        btnSignIn = (Button)findViewById( R.id.btnSignIn );
        ckbRemember = (CheckBox)findViewById( R.id.ckbRemember );
        txtForgotPwd = (TextView) findViewById( R.id.txtForgotPwd );

        // Init Paper
        Paper.init( this );

        //Init Firebase
        database = FirebaseDatabase.getInstance();
        table_user = database.getReference("User");

        txtForgotPwd.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showForgotPwdDialog();
            }
        } );

        btnSignIn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Common.isConnectedToInternet( getBaseContext() )) {

                    // Save user & password
                    if (ckbRemember.isChecked()) {
                        Paper.book().write( Common.USER_KEY, editPhone.getText().toString() );
                        Paper.book().write( Common.PWD_KEY, editPassword.getText().toString() );
                    }

                    final ProgressDialog mDialog = new ProgressDialog( SignIn.this );
                    mDialog.setMessage( "Molimo sačekajte..." );
                    mDialog.show();

                    table_user.addListenerForSingleValueEvent( new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            //Check if user not exist in database
                            if (dataSnapshot.child( editPhone.getText().toString() ).exists()) {
                                //Get User information
                                mDialog.dismiss();
                                User user = dataSnapshot.child( editPhone.getText().toString() ).getValue( User.class );
                                user.setPhone( editPhone.getText().toString() ); // set Phone
                                if (user.getPassword().equals( editPassword.getText().toString() )) {
                                    Toast.makeText( SignIn.this, "Uspešno logovanje!", Toast.LENGTH_SHORT ).show();
                                    Intent homeIntent = new Intent( SignIn.this, Home.class );
                                    Common.currentUser = user;
                                    startActivity( homeIntent );
                                    finish();

                                    table_user.removeEventListener( this );

                                } else {
                                    Toast.makeText( SignIn.this, "Pogrešna lozinka!!!", Toast.LENGTH_SHORT ).show();
                                }
                            } else {
                                mDialog.dismiss();
                                Toast.makeText( SignIn.this, "Korisnik ne postoji!", Toast.LENGTH_SHORT ).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    } );
                }
                else {
                    Toast.makeText( SignIn.this, "Proverite internet konekciju!", Toast.LENGTH_SHORT ).show();
                    return;
                }
            }

        } );
    }

    private void showForgotPwdDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setTitle( "Zaboravljena lozinka" );
        builder.setMessage( "Unesite svoj sigurnosni kod" );

        LayoutInflater inflater = this.getLayoutInflater();
        View forgot_view = inflater.inflate( R.layout.forgot_password_layout, null );

        builder.setView( forgot_view );
        builder.setIcon( R.drawable.ic_security_black_24dp );

        final MaterialEditText editPhone = (MaterialEditText)forgot_view.findViewById( R.id.editPhone );
        final MaterialEditText editSecureCode = (MaterialEditText)forgot_view.findViewById( R.id.editSecureCode );

        builder.setPositiveButton( "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Check if user available
                table_user.addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.child( editPhone.getText().toString() )
                                .getValue(User.class);

                        if (user.getSecureCode().equals( editSecureCode.getText().toString() ))
                            Toast.makeText( SignIn.this, "Vaša lozinka: "+user.getPassword(), Toast.LENGTH_LONG ).show();
                        else
                            Toast.makeText( SignIn.this, "Pogrešan sigurnosni kod!", Toast.LENGTH_SHORT ).show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                } );
            }
        } );
        builder.setNegativeButton( "Odustajem", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        } );

        builder.show();
    }
}
