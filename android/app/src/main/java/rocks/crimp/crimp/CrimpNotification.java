package rocks.crimp.crimp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
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
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack
        stackBuilder.addParentStack(TaskListActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(intent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

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
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack
        stackBuilder.addParentStack(TaskListActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(intent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

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
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack
        stackBuilder.addParentStack(TaskListActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(intent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

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
