package com.tfg.jose.proteccionpersonas;

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

    private Inicio inicio;

    public Notification(Inicio ini){
        this.inicio = ini;
    }

    // Vibración
    void vibrar(){
        Vibrator v = (Vibrator) inicio.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(800);
    }

    // Crear y enviar notificacion
    void notificar_radio(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(inicio);

        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(inicio, 0, intent, 0);

        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.drawable.notification);
        builder.setContentTitle("¡AVISO!");
        builder.setContentText("El agresor ha sido detectado.");

        NotificationManager notificationManager = (NotificationManager) inicio.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(1, builder.build());
    }

    // Crear y enviar notificacion
    void notificar_limite(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(inicio);

        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(inicio, 0, intent, 0);

        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.drawable.notification);
        builder.setContentTitle("¡PELIGRO!");
        builder.setContentText("El agresor ha superado la distancia límite.");

        NotificationManager notificationManager = (NotificationManager) inicio.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(1, builder.build());
    }

}
