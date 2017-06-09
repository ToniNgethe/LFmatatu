package com.example.toni.lfmatatu;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {

    public static final String _MATATU = "MATATU_KEY";
    private Button submit;
    private EditText email, password;

    private DatabaseReference mUsers;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mUsers = FirebaseDatabase.getInstance().getReference().child("Matatu_Credentials");
        checkSession();

        //views
        email = (EditText) findViewById(R.id.et_signin_email);
        password = (EditText) findViewById(R.id.et_signin_pass);
        submit = (Button) findViewById(R.id.btn_sigin_login);

        //firebase


        //sign in
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!TextUtils.isEmpty(email.getText()) && !TextUtils.isEmpty(password.getText())) {

                    final SweetAlertDialog pDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                    pDialog.getProgressHelper().setBarColor(Color.parseColor("#f50057"));
                    pDialog.setTitleText("authenticating user...");
                    pDialog.setCancelable(false);
                    pDialog.show();

                    mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {

                                //check database for driver
                                mUsers.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.exists()) {
                                            pDialog.dismiss();

                                            if (dataSnapshot.child("role").getValue().toString().equals("driver")) {
                                                String mat_key = dataSnapshot.child("mat").getValue().toString();
                                                Intent i = new Intent(MainActivity.this, DriverActivity.class);
                                                i.putExtra(_MATATU, mat_key);
                                                startActivity(i);
                                                finish();

                                                //redirect to driver

                                            } else {
                                                String mat_key = dataSnapshot.child("matatu").getValue().toString();
                                                //redirect to conductor...
                                                Intent i = new Intent(MainActivity.this, ConductorActivity.class);
                                                i.putExtra(_MATATU, mat_key);
                                                startActivity(i);
                                                finish();
                                            }

                                        } else {
                                            new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                                                    .setTitleText("Oops...")
                                                    .setContentText("Detail not found..contact your sacco")
                                                    .show();
                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });


                            } else {
                                pDialog.dismiss();
                                new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Oops...")
                                        .setContentText(task.getException().getMessage())
                                        .show();

                            }

                        }
                    });

                } else {
                    Toast.makeText(MainActivity.this, "Field(s) empty", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void checkSession() {
        if (mAuth.getCurrentUser() != null) {
            final SweetAlertDialog pDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#f50057"));
            pDialog.setTitleText("authenticating user...");
            pDialog.setCancelable(false);
            pDialog.show();
            mUsers.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        pDialog.dismiss();

                        if (dataSnapshot.child("role").getValue().toString().equals("driver")) {
                            String mat_key = dataSnapshot.child("mat").getValue().toString();
                            Intent i = new Intent(MainActivity.this, DriverActivity.class);
                            i.putExtra(_MATATU, mat_key);
                            startActivity(i);
                            finish();

                            //redirect to driver

                        } else {
                            String mat_key = dataSnapshot.child("matatu").getValue().toString();
                            //redirect to conductor...
                            Intent i = new Intent(MainActivity.this, ConductorActivity.class);
                            i.putExtra(_MATATU, mat_key);
                            startActivity(i);
                            finish();
                        }

                    } else {
                        new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Oops...")
                                .setContentText("Detail not found..contact your sacco")
                                .show();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
