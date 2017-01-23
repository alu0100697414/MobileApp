package com.tfg.jose.proteccionpersonas.disuse;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.tfg.jose.proteccionpersonas.R;
import com.tfg.jose.proteccionpersonas.main.BackgroundVideoRecorder;
import com.tfg.jose.proteccionpersonas.main.Inicio;
import com.tfg.jose.proteccionpersonas.main.Notification;

import java.text.DecimalFormat;

/**
 * Created by jose on 29/01/16.
 *
 * Clase que se encarga de la búsqueda de dispositivos a través de Bluetooth.
 *
 * LOG: Clase en desuso desde el 22/10/2016. Ahora se detecta al dispositivo
 * usando BLE en vez de las librerías clásicas de Bluetooh.
 */

public class BluetoothConnection {

    private Context mContext;
    private Activity mActivity;

    private Notification notifi;

    private boolean deviceFound; // True si encuentra el dispositivo

    private String nombre_dispositivo;
    private String direccion_dispositivo;
    private int distancia_limite;

    private double px;  // Valor de intensidad de señal entre dos dispositivos a un metro de distancia.

    // El adaptador bluetooth tiene que ser final y por tanto inicializado aquí
    private final BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();

    // Constructor
    public BluetoothConnection(Context context, Activity activity){
        this.mContext = context;
        this.mActivity = activity;

        this.notifi = new Notification(context, activity);

        this.deviceFound = false;

        this.px = -54;

//        this.nombre_dispositivo = "jose-TravelMate-5742-0";
        this.nombre_dispositivo = "DESKTOP-GACG2NK";
        this.direccion_dispositivo = "00:15:83:E4:D7:86";
        this.distancia_limite = 10;
    }

    // Se llama al receiver cada vez que encuentra un dispositivo nuevo
    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action)) {

                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);

                // Si encuentra el dispositivo del agresor/a entra:
                if(name != null && name.equals(nombre_dispositivo)){

                    // Calculamos la distancia aproximada
                    double rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                    double distance = getDistance(rssi, px);

                    deviceFound = true; // Encontró el dispositivo

                    // Parseamos el resultado para que muestre dos decimales
                    DecimalFormat df = new DecimalFormat("#.##");
                    String rdistance = df.format(distance);

                    TextView rssi_msg = (TextView) mActivity.findViewById(R.id.res_busqueda);
                    TextView res_dist = (TextView) mActivity.findViewById(R.id.res_distancia);

                    // Si está dentro de la distancia límite se le avisa
                    if(distance < getDistancia_limite()){
                        rssi_msg.setText(context.getString(R.string.peligro) + "\n" + context.getString(R.string.mensaje_peligro));
                        res_dist.setText(rdistance + "m");

                        // Notificación del límite superado
                        notifi.notificar_limite();

                        // Envío de aviso a los contactos
                        notifi.enviar_sms();
                        notifi.setSms_enviado(1);

                        // Abre la activity, si esta cerrada, con los resultados
                        if(mActivity.hasWindowFocus() == false) {
                            Intent intento = new Intent(mContext, Inicio.class);
                            intento.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            mContext.startActivity(intento);
                        }

                        // Iniciamos el servicio con la grabación de vídeo
                        if(isMyServiceRunning(BackgroundVideoRecorder.class) == false){
                            mContext.startService(new Intent(mContext, BackgroundVideoRecorder.class));
                        }

                        TextView grabando = (TextView) mActivity.findViewById(R.id.grabando);
                        grabando.setCompoundDrawablesWithIntrinsicBounds(R.drawable.grabando, 0, 0, 0);
                        grabando.setVisibility(View.VISIBLE);
                    }

                    // Si lo encuentra pero no la supera, se le dice
                    else {
                        rssi_msg.setText(context.getString(R.string.mensaje_aviso));
                        res_dist.setText(rdistance + "m");

                        // Notificación de que se encuentra por los alrededores
                        notifi.notificar_radio();
                    }

                    // Finalizamos la búsqueda si lo encontramos
                    BTAdapter.cancelDiscovery();
                    mContext.stopService(new Intent(mContext, BService.class));

                    mActivity.invalidateOptionsMenu(); // Refrescamos el menú
                }

            }
        }
    };

    // Devuelve la distancia aproximada en metros entre dos dispositivos
    double getDistance(double rssi, double txPower) {
        // El 2.7 es el valor de n y si no hay obstáculos de por medio se usa el valor 2
        return Math.pow(10d, ((double) txPower - rssi) / (10 * 2.7));
    }

    // Si está desactivado el Bluetooth, enviamos mensaje para activarlo
    void estaActivado(){
        if (!BTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(enableBtIntent, 1);
        }
    }

    // Si el adaptador ya está buscando dispositivos, lo paramos
    void estaBuscando(){
        if (BTAdapter.isDiscovering()) {
            BTAdapter.cancelDiscovery();
        }
    }

    // Activamos la busqueda del dispositivo
    void buscar(){
        BTAdapter.startDiscovery();
    }

    // Si no encuentra nada tras doce segundos
    void sinPeligro(){
        //delay in ms
        int DELAY = 12000;

        // Si cuando se acaba la búsqueda, no lo encontró, no hay peligro
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                TextView rssi_msg = (TextView) mActivity.findViewById(R.id.res_busqueda);
                TextView rssi_dist = (TextView) mActivity.findViewById(R.id.res_distancia);

                mActivity.invalidateOptionsMenu(); // Refrescamos el menu

                if (deviceFound == false && BTAdapter.isEnabled()) {
                    BTAdapter.cancelDiscovery();

                    mContext.stopService(new Intent(mContext, BService.class));

                    notifi.setSms_enviado(0); // Acutlizamos a 0 para si vuelve a encotnrar al agresor

                    rssi_msg.setText(mContext.getString(R.string.sin_peligro));
                    rssi_dist.setText("");

                } else if (deviceFound == false && !BTAdapter.isEnabled()) {
                    BTAdapter.cancelDiscovery();

                    mContext.stopService(new Intent(mContext, BService.class));

                    rssi_msg.setText(mContext.getString(R.string.b_desactivado));
                    rssi_dist.setText("");

                    notifi.bluetooth_desactivado();
                } else {
                    deviceFound = false;
                }
            }
        }, DELAY);
    }

    boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // Devuelve el nombre del dispositivo
    String getNombre_dispositivo(){
        return nombre_dispositivo;
    }

    // Devuelve la dirección del dispositivo
    String getDireccion_dispositivo(){
        return direccion_dispositivo;
    }

    // Devuelve la distancia limite a la que se puede acercar el agresor/a
    int getDistancia_limite(){
        return distancia_limite;
    }

    // Devuelve el receiver
    BroadcastReceiver getReceiver(){
        return receiver;
    }

    // Devuelve el adaptador Bluetooth
    BluetoothAdapter getBTAdapter(){
        return BTAdapter;
    }

    // Devuelve si encontró el dispositivo
    boolean getdeviceFound(){
        return deviceFound;
    }
}
