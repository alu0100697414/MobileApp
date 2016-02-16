package com.tfg.jose.proteccionpersonas;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v7.app.NotificationCompat;

/**
 * Created by jose on 29/01/16.
 */
public class Notification {

    private Context mContext;
    private Activity mActivity;

    public Notification(Context context, Activity activity){
        this.mContext = context;
        this.mActivity = activity;
    }

    // Vibración
    void vibrar(){
        Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(800);
    }

    // Crear y enviar notificacion
    void notificar_radio(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);

        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.drawable.notification);
        builder.setContentTitle("¡AVISO!");
        builder.setContentText("El agresor ha sido detectado.");

        NotificationManager notificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(1, builder.build());
    }

    // Crear y enviar notificacion
    void notificar_limite(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);

        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.drawable.notification);
        builder.setContentTitle("¡PELIGRO!");
        builder.setContentText("El agresor ha superado la distancia límite.");

        NotificationManager notificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(1, builder.build());
    }

}
