package com.edirnehavadurumu.app;

import android.app.Application;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessaging;

/** Initializes the public broadcast topic for all app installations. */
public class EdirneApplication extends Application {
    private static final String TAG = "EdirneBroadcast";

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseMessaging.getInstance().subscribeToTopic("all_users")
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.i(TAG, "Subscribed to all_users");
                } else {
                    Log.w(TAG, "Could not subscribe to all_users", task.getException());
                }
            });
    }
}
