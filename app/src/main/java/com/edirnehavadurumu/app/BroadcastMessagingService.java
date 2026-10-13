package com.edirnehavadurumu.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;

/** Displays broadcast messages received while the app is in the foreground. */
public class BroadcastMessagingService extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "edirne_announcements";

    @Override
    public void onMessageReceived(RemoteMessage message) {
        Map<String, String> data = message.getData();
        String title = message.getNotification() != null
                ? message.getNotification().getTitle() : data.get("title");
        String body = message.getNotification() != null
                ? message.getNotification().getBody() : data.get("body");
        if (title == null || title.trim().isEmpty()) title = "Edirne Hava Durumu";
        if (body == null || body.trim().isEmpty()) return;

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Genel Duyurular", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Edirne Hava Durumu genel duyuruları");
            manager.createNotificationChannel(channel);
        }

        Intent launch = new Intent(this, MainActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pending = PendingIntent.getActivity(
                this, 0, launch,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(pending)
                .build();
        manager.notify((int) (System.currentTimeMillis() & 0x7fffffff), notification);
    }
}
