package com.tfg.jose.proteccionpersonas;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
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

    // Notificación cuando es detectado pero no sobreapasa el radio.
    void notificar_radio(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);

        Intent intent = new Intent(mContext, Inicio.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.drawable.notification);
        builder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
        builder.setAutoCancel(true);
        builder.setContentTitle("¡AVISO!");
        builder.setContentText("El agresor ha sido detectado.");

        String texto = "El agresor ha sido detectado pero no ha superado la distancia límite.";
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(texto));

        NotificationManager notificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    // Notificación cuando sobrepasa el radio.
    void notificar_limite(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);

        Intent intent = new Intent(mContext, Inicio.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.drawable.notification);
        builder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
        builder.setAutoCancel(true);
        builder.setContentTitle("¡PELIGRO!");
        builder.setContentText("Distancia límite superada.");

        String texto = "El agresor ha sido detectado y además, superó la distancia límite.";
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(texto));

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(alarmSound);

        NotificationManager notificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

}
