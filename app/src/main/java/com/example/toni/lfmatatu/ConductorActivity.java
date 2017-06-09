package com.example.toni.lfmatatu;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.toni.lfmatatu.Dialog.TicketInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ConductorActivity extends AppCompatActivity {

    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private Button scan;
    private TextView  plate;
    private ImageView profile;
    private String matKey;

    private DatabaseReference mMatatu;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conductor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //views
        scan = (Button) findViewById(R.id.btn_conductor);
        plate = (TextView) findViewById(R.id.tv_conductor_plate);
        profile = (ImageView) findViewById(R.id.iv_conductor);

        //get matatu key
        matKey = getIntent().getExtras().getString(MainActivity._MATATU);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null){
                    startActivity(new Intent(ConductorActivity.this, MainActivity.class));
                }
            }
        };

        mMatatu = FirebaseDatabase.getInstance().getReference().child("Matatu").child(matKey);
        mMatatu.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    plate.setText(dataSnapshot.child("plate").getValue().toString());
                    //Op
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(ConductorActivity.this).load(dataSnapshot.child("conductor").getValue().toString())
                                    .crossFade()
                                    .thumbnail(0.5f)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .error(R.drawable.ic_user)
                                    .into(profile);
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginProcess();

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    private void beginProcess() {

        IntentIntegrator integrator = new IntentIntegrator(ConductorActivity.this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Scan Ticket");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {

            if (result.getContents() == null) {
                Toast.makeText(this, "You cancelled the scanning", Toast.LENGTH_LONG).show();
            } else {
                //Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();

                final SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
                pDialog.getProgressHelper().setBarColor(Color.parseColor("#00bcd4"));
                pDialog.setTitleText("Checking ticket...");
                pDialog.setCancelable(false);
                pDialog.show();


                DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Tickets");
                db.child(result.getContents()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {


                        if (dataSnapshot.exists()) {

                            //check if used or not
                            if (Integer.valueOf(dataSnapshot.child("status").getValue().toString()) == 0) {


                                DatabaseReference dc = FirebaseDatabase.getInstance().getReference().child("Matatu");

                                // mMatatu.child(dataSnapshot.child("matatu").getValue().toString());
                                // Query q = dc.orderByChild("matatu").equalTo(dataSnapshot.child("matatu").getValue().toString());

                                if (dataSnapshot.child("matatu").getValue().toString().equals(matKey)) {

                                    dc.child(dataSnapshot.child("matatu").getValue().toString()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot ds) {
                                            pDialog.dismiss();

                                            // Toast.makeText(ConductorActivity.this,ds.toString(),Toast.LENGTH_LONG).show();
                                            // Log.d("sdfsffdsd",  image[0]);

                                            if (ds.exists()) {

                                                DatabaseReference user = FirebaseDatabase.getInstance().getReference().child("Users");
                                                user.child(dataSnapshot.child("user").getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnap) {

                                                        TicketInfo ticketInfo = new TicketInfo(ConductorActivity.this, dataSnap.child("image").getValue().toString(),
                                                                dataSnapshot.child("sits").getValue().toString(), dataSnapshot.child("total").getValue().toString(), result.getContents());
                                                        ticketInfo.setCanceledOnTouchOutside(false);
                                                        ticketInfo.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;
                                                        ticketInfo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                                        ticketInfo.show();
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });


                                            } else {

                                                new SweetAlertDialog(ConductorActivity.this, SweetAlertDialog.ERROR_TYPE)
                                                        .setTitleText("Ticket not for this matatu")
                                                        .show();
                                            }

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                } else {
                                    pDialog.dismissWithAnimation();
                                    new SweetAlertDialog(ConductorActivity.this, SweetAlertDialog.ERROR_TYPE)
                                            .setTitleText("Ticket not for this matatu")
                                            .show();
                                }
                            } else {
                                pDialog.dismissWithAnimation();
                                new SweetAlertDialog(ConductorActivity.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Ticket has already been used")
                                        .show();
                            }

                        } else {
                            pDialog.dismissWithAnimation();
                            new SweetAlertDialog(ConductorActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Not a valid Ticket")
                                    .show();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        } else {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.close_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.con_action_logout:

                    mAuth.signOut();

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }

}
