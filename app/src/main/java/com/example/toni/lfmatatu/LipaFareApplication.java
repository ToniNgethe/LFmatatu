package com.example.toni.lfmatatu;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by toni on 3/24/17.
 */

public class LipaFareApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        if (!FirebaseApp.getApps(this).isEmpty()) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        }

    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


}
