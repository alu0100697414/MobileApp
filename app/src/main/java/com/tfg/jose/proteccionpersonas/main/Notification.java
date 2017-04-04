package com.tfg.jose.proteccionpersonas.main;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.telephony.SmsManager;
import android.text.format.Time;

import com.tfg.jose.proteccionpersonas.R;

import java.util.ArrayList;


/**
 * Created by jose on 29/01/16.
 *
 * Clase que tiene los métodos para las diferentes notificaciones que hagan falta en la app.
 */
public class Notification {

    private Context mContext;
    private Activity mActivity;

    private int sms_enviado; // 0:no 1:si

    private DBase protectULLDB;

    public Notification(Context context, Activity activity){
        this.mContext = context;
        this.mActivity = activity;

        this.sms_enviado = 0;
    }

    // Notificación cuando la vítima tiene el bluetooth desactivado
    public void bluetooth_desactivado(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);

        Intent intent = new Intent(mContext, Inicio.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.drawable.notification);
        builder.setAutoCancel(true);
        builder.setOngoing(true);
        builder.setContentTitle(mContext.getString(R.string.aviso));
        builder.setContentText(mContext.getString(R.string.b_desactivado));

        String texto = mContext.getString(R.string.msg_b_desactivado_1) + " " + mContext.getString(R.string.msg_b_desactivado_2);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(texto));

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(alarmSound);

        NotificationManager notificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    // Notificación cuando la vítima tiene el GPS desactivado
    public void gps_desactivado(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);

        Intent intent = new Intent(mContext, Inicio.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.drawable.notification);
        builder.setAutoCancel(true);
        builder.setOngoing(true);
        builder.setContentTitle(mContext.getString(R.string.aviso));
        builder.setContentText(mContext.getString(R.string.gps_desactivado));

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(alarmSound);

        NotificationManager notificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    // Notificación cuando la vítima tiene el GPS desactivado
    public void gps_error(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);

        Intent intent = new Intent(mContext, Inicio.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.drawable.notification);
        builder.setAutoCancel(true);
        builder.setContentTitle(mContext.getString(R.string.aviso));
        builder.setContentText(mContext.getString(R.string.gps_error));

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(alarmSound);

        NotificationManager notificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    // Notificación cuando es detectado pero no sobreapasa el radio.
    public void notificar_radio(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);

        Intent intent = new Intent(mContext, Inicio.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.drawable.notification);
        builder.setVibrate(new long[]{1000, 1000});
        builder.setAutoCancel(true);
        builder.setContentTitle(mContext.getString(R.string.aviso));
        builder.setContentText(mContext.getString(R.string.msg_agresor_detectado));

        String texto = mContext.getString(R.string.text_agresor_detectado);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(texto));

        NotificationManager notificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    // Notificación cuando sobrepasa el radio.
    public void notificar_limite(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);

        Intent intent = new Intent(mContext, Inicio.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.drawable.notification);
        builder.setVibrate(new long[]{1000, 1000});
        builder.setAutoCancel(true);
        builder.setContentTitle(mContext.getString(R.string.peligro));
        builder.setContentText(mContext.getString(R.string.dis_limite_superada));

        String texto = mContext.getString(R.string.text_agresor_peligro);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(texto));

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(alarmSound);

        NotificationManager notificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    // Notificación cuando sobrepasa el radio.
    void notificar_grabacion(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);

        Intent intent = new Intent(mContext, Inicio.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.drawable.grabando);
        builder.setAutoCancel(true);
        builder.setContentTitle(mContext.getString(R.string.grabando_puntos));
        builder.setContentText(mContext.getString(R.string.grabacion_encurso));

        NotificationManager notificationManager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    // Funcion que envía los sms
    public void enviar_sms(){

        protectULLDB = new DBase(mContext.getApplicationContext());

        Time now = new Time(Time.getCurrentTimezone());
        now.setToNow();

        String minute;
        String hour;

        int min = now.minute; if(min < 10){minute = "0" + String.valueOf(min);} else { minute = String.valueOf(min); }
        int hou = now.hour; if(hou < 10){hour = "0" + String.valueOf(hou);} else { hour = String.valueOf(hou); }


        String mensaje = mContext.getString(R.string.aviso) + " " + mContext.getString(R.string.a_las) +  " " + hour + ":" + minute + " " + mContext.getString(R.string.del_dia) + " " + now.monthDay + "/" + now.month + "/" + now.year + " " + mContext.getString(R.string.msg_sms);

        SmsManager sms = SmsManager.getDefault();

        ArrayList<String> parts = sms.divideMessage(mensaje);

        if(protectULLDB.recuperarCONTACTOS().size() != 0 && sms_enviado == 0){
            for (int i = 0; i < protectULLDB.recuperarCONTACTOS().size(); i++) {
                if(protectULLDB.recuperarCONTACTOS().get(i).getActive() == 1){
                    sms.sendMultipartTextMessage(protectULLDB.recuperarCONTACTOS().get(i).getNumber(), null, parts, null, null);
                }
            }
        }

    }

    int getSms_enviado(){
        return sms_enviado;
    }

    public void setSms_enviado(int n){
        sms_enviado = n;
    }
}

