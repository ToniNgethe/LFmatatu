package com.example.toni.lfmatatu;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class DriverActivity extends AppCompatActivity {

    private Button clockIN;
    private ImageView profile;
    private TextView plate, sits_remaingin;

    private DatabaseReference mMatatu;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private String matKey, sacco, sits;
    private NotificationManager notificationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Driver");
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //views
        clockIN = (Button) findViewById(R.id.button_driver_clockin);
        profile = (ImageView) findViewById(R.id.circleImageView_driver);
        plate = (TextView) findViewById(R.id.tv_driver_plate);
        sits_remaingin = (TextView) findViewById(R.id.tv_driver_sits);


        //get matatu key
        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser()==null){

                    startActivity(new Intent(DriverActivity.this, MainActivity.class));

                }
            }
        };

        matKey = getIntent().getExtras().getString(MainActivity._MATATU);
        final DatabaseReference mQueue = FirebaseDatabase.getInstance().getReference().child("Queue").child(matKey);


        //check number of sits remaingin...
        mQueue.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    if (dataSnapshot.exists()) {
                        sits_remaingin.setText(dataSnapshot.child("sits").getValue().toString() + " sits remaining");

                        if (Integer.valueOf(dataSnapshot.child("sits").getValue().toString()) == 0){
                            //dataSnapshot.getRef().removeValue();
                            sendNotification("Matatu/Bus full");
                            Toast.makeText(DriverActivity.this, "Bus/Matatu full..", Toast.LENGTH_SHORT).show();
                            dataSnapshot.getRef().removeValue();
                        }

                    } else {
                        sits_remaingin.setText("Not clocked in");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //firebase
        mMatatu = FirebaseDatabase.getInstance().getReference().child("Matatu").child(matKey);
        mMatatu.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    plate.setText(dataSnapshot.child("plate").getValue().toString());
                    sacco = dataSnapshot.child("sacco").getValue().toString();
                    sits = dataSnapshot.child("sits").getValue().toString();
                    //Op
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(DriverActivity.this).load(dataSnapshot.child("driver").getValue().toString())
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

        clockIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final SweetAlertDialog pDialog = new SweetAlertDialog(DriverActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                pDialog.getProgressHelper().setBarColor(Color.parseColor("#f50057"));
                pDialog.setTitleText("Adding matatu/bus to queue...");
                pDialog.setCancelable(false);
                pDialog.show();

                mQueue.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (!dataSnapshot.exists()){

                            dataSnapshot.child("sacco").getRef().setValue(sacco);
                            dataSnapshot.child("sits").getRef().setValue(Integer.valueOf(sits)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    pDialog.dismissWithAnimation();

                                    if (task.isSuccessful()){

                                        new SweetAlertDialog(DriverActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                                .setTitleText("Successfully added to queue...")
                                                .show();


                                    }else {
                                        new SweetAlertDialog(DriverActivity.this, SweetAlertDialog.ERROR_TYPE)
                                                .setTitleText("Oops...")
                                                .setContentText(task.getException().getMessage())
                                                .show();
                                    }
                                }
                            });

                        }else {
                            pDialog.dismissWithAnimation();
                            new SweetAlertDialog(DriverActivity.this, SweetAlertDialog.NORMAL_TYPE)
                                    .setTitleText("Matatu/Bus already in queue")
                                    .show();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
    }

    private void sendNotification(String messageBody) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Sits Notification")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setTicker("Matatu/Bus already full..")
                .setSound(defaultSoundUri);


        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int id = 0;
        notificationManager.notify(id, notificationBuilder.build());
        removeNotification(id);
    }
    private void removeNotification(final int id) {
        Handler handler = new Handler();
        long delayInMilliseconds = 10000;
        handler.postDelayed(new Runnable() {
            public void run() {
                notificationManager.cancel(id);
            }
        }, delayInMilliseconds);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.driver_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_logout:

                    mAuth.signOut();
                  //  finish();
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
