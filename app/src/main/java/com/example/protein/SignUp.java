package com.example.protein;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.rengwuxian.materialedittext.MaterialEditText;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignUp extends AppCompatActivity {

    MaterialEditText editPhone, editName, editPassword, editSecureCode;
    Button btnSignUp;

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

        setContentView( R.layout.activity_sign_up );

        editName = (MaterialEditText)findViewById( R.id.editName );
        editPhone = (MaterialEditText)findViewById( R.id.editPhone );
        editPassword = (MaterialEditText)findViewById( R.id.editPassword );
        editSecureCode = (MaterialEditText)findViewById( R.id.editSecureCode );

        btnSignUp = (Button)findViewById( R.id.btnSignUp );

        //Init Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        btnSignUp.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Common.isConnectedToInternet( getBaseContext() )) {

                    final ProgressDialog mDialog = new ProgressDialog( SignUp.this );
                    mDialog.setMessage( "Molimo sačekajte..." );
                    mDialog.show();

                    table_user.addValueEventListener( new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Check if already user phone
                            if (dataSnapshot.child( editPhone.getText().toString() ).exists()) {
                                mDialog.dismiss();
                                Toast.makeText( SignUp.this, "Broj telefona već postoji!", Toast.LENGTH_SHORT ).show();
                            } else {
                                mDialog.dismiss();
                                User user = new User( editName.getText().toString(),
                                        editPassword.getText().toString(),
                                        editSecureCode.getText().toString());
                                table_user.child( editPhone.getText().toString() ).setValue( user );
                                Toast.makeText( SignUp.this, "Uspešno ste se registrovali!", Toast.LENGTH_SHORT ).show();
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    } );
                }
                else {
                    Toast.makeText( SignUp.this, "Proverite internet konekciju!", Toast.LENGTH_SHORT ).show();
                    return;
                }

            }
        } );

    }
}
