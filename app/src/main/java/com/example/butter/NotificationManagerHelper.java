
package com.example.butter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * Used to manage notifications.
 * Handles displaying the notification as well
 * @author Ahmer
 */
public class NotificationManagerHelper {
    private static final String CHANNEL_ID = "butter_notifications";

    public static void handleNotification(Context context, String title, String body) {
        // Check if permission is required
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission(context);
                return;
            }
        }

        // Build and display the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.calendar)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private static void requestNotificationPermission(Context context) {
        if (context instanceof ActivityCompat.OnRequestPermissionsResultCallback) {
            // Request the POST_NOTIFICATIONS permission
            ActivityCompat.requestPermissions(
                    (Activity) context,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    101 // Request code to identify this request
            );
        }
    }
}
