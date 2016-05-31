package rocks.crimp.crimp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import rocks.crimp.crimp.tasklist.TaskListActivity;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CrimpNotification {
    public static final int NOTIFICATION_ID = 1;

    public static void createAndShowCompleted(Context context){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context);

        Intent intent = new Intent(context, TaskListActivity.class);
        Intent[] intentArray = {intent};
        PendingIntent pendingIntent = PendingIntent.getActivities(context, 0, intentArray,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setSmallIcon(R.drawable.notification)
                .setContentTitle("Score upload completed!")
                .setContentText("Awesome!")
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        // Builds the notification and issues it.
        mNotifyMgr.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public static void createAndShowUploading(Context context, int count){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context);

        Intent intent = new Intent(context, TaskListActivity.class);
        Intent[] intentArray = {intent};
        PendingIntent pendingIntent = PendingIntent.getActivities(context, 0, intentArray,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setSmallIcon(R.drawable.notification)
                .setContentTitle("Uploading scores")
                .setContentText(count+" score upload tasks remaining")
                .setOngoing(true)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent);

        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        // Builds the notification and issues it.
        mNotifyMgr.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public static void createAndShowError(Context context, String message){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context);

        Intent intent = new Intent(context, TaskListActivity.class);
        Intent[] intentArray = {intent};
        PendingIntent pendingIntent = PendingIntent.getActivities(context, 0, intentArray,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setSmallIcon(R.drawable.notification)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle("Upload paused")
                .setContentText(message)
                .setOngoing(true)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent);

        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        // Builds the notification and issues it.
        mNotifyMgr.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
