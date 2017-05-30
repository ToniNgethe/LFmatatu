package com.example.toni.lfmatatu.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.toni.lfmatatu.ConductorActivity;
import com.example.toni.lfmatatu.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by toni on 5/30/17.
 */

public class TicketInfo extends Dialog {

    private String image,sits,plate,ticket;
    private Context ctx;

    public TicketInfo(@NonNull Context context, String image, String sits, String plate, String ticket) {
        super(context);

        this.ctx  =context;
        this.image = image;
        this.sits = sits;
        this.plate = plate;
        this.ticket = ticket;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_ticket);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_user_process);
        TextView sist = (TextView) findViewById(R.id.tv_user_sits);
        TextView plate_n = (TextView) findViewById(R.id.iv_user_total);
        final ImageView proile = (ImageView) findViewById(R.id.iv_user_image);


        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Glide.with(ctx).load(image)
                        .crossFade()
                        .thumbnail(0.5f)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(R.drawable.ic_user)
                        .into(proile);
            }
        });

        sist.setText(sits + " sits");
        plate_n.setText(plate + " ksh");

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final DatabaseReference mTicks = FirebaseDatabase.getInstance().getReference().child("Tickets");
                mTicks.child(ticket).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            dataSnapshot.child("status").getRef().setValue(1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        new SweetAlertDialog(ctx, SweetAlertDialog.SUCCESS_TYPE)
                                                .setTitleText("Ticket Processed successfully")
                                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                    @Override
                                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                        dismiss();
                                                        sweetAlertDialog.dismissWithAnimation();
                                                    }
                                                })
                                                .show();
                                    }else {



                                    }
                                }
                            });


                        }else {
                            Toast.makeText(ctx, "This ticket has not been found", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

    }
}
