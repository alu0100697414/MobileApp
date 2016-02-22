package com.tfg.jose.proteccionpersonas;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by jose on 29/01/16.
 *
 * Clase que se encarga de la búsqueda de dispositivos a través de Bluetooth.
 */

public class BluetoothConnection {

    private Context mContext;
    private Activity mActivity;

    private Notification notifi;
    private RCamera rcamera;

    private boolean deviceFound; // True si encuentra el dispositivo

    private String nombre_dispositivo;
    private String direccion_dispositivo;
    private int distancia_limite;

    private double px;  // Valor de intensidad de señal entre dos dispositivos a un metro de distancia.

    ScheduledExecutorService pausa;

    // El adaptador bluetooth tiene que ser final y por tanto inicializado aquí
    private final BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();

    // Constructor
    public BluetoothConnection(Context context, Activity activity){
        this.mContext = context;
        this.mActivity = activity;

        this.notifi = new Notification(context, activity);

        this.deviceFound = false;

        this.px = -54;

        this.nombre_dispositivo = "jose-TravelMate-5742-0";
        this.direccion_dispositivo = "00:15:83:E4:D7:86";
        this.distancia_limite = 10;

        this.pausa = Executors.newScheduledThreadPool(1);

    }

    // Se llama al receiver cada vez que encuentra un dispositivo nuevo
    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action)) {

                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);

                // Si encuentra el dispositivo del agresor/a entra:
                if(name.equals(nombre_dispositivo)){

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
                        rssi_msg.setText("¡PELIGRO!\nSe ha superado la distancia límite. El agresor se encuentra a una distancia aproximada de:");
                        res_dist.setText(rdistance + "m");

                        // Notificación del límite supe
                        notifi.notificar_limite();

//                        // Abre la activity, si esta cerrada, con los resultados
//                        if(mActivity.hasWindowFocus() == false) {
//                            Intent intento = new Intent(mContext, Inicio.class);
//                            intento.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                            mContext.startActivity(intento);
//                        }
//
//                        else {
//                            recordON();
//                        }

                        // Abre la activity, si esta cerrada, con los resultados
                        if(mActivity.hasWindowFocus() == false) {
                            Intent intento = new Intent(mContext, Inicio.class);
                            intento.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            mContext.startActivity(intento);

                            // Ejecutamos el servicio de busqueda de dispositivos bluetooth cada x tiempo
                            Runnable pausar = new Runnable() {
                                public void run() {
                                    if(mActivity.hasWindowFocus() == true){
                                        if(rcamera == null) {
                                            rcamera = new RCamera(mContext,mActivity);
                                        }

                                        if(rcamera.getCameraState() == false){
                                            try {
                                                rcamera.startRecording();
                                            }

                                            catch (IOException e) {
                                                String message = e.getMessage();
                                                Log.i(null, "Problem Start" + message);
                                                rcamera.getMrec().release();
                                            }
                                        }

                                        pausa.shutdown();
                                    }
                                }
                            };

                            pausa.scheduleAtFixedRate(pausar, 0, 1, TimeUnit.SECONDS);
                        }

                        else {
                            recordON();
                        }
                    }

                    // Si lo encuentra pero no la supera, se le dice
                    else {
                        rssi_msg.setText("Fuera de la distancia de peligro.");
                        res_dist.setText(rdistance + "m");

                        // Notificación de que se encuentra por los alrededores
                        notifi.notificar_radio();
                    }

                    // Finalizamos la búsqueda si lo encontramos
                    BTAdapter.cancelDiscovery();
                    mContext.stopService(new Intent(mContext, BService.class));
                }

            }
        }
    };

    // Devuelve la distancia aproximada en metros entre dos dispositivos
    double getDistance(double rssi, double txPower) {
        // El 4 es el valor de n y si no hay obstáculos de por medio se usa el valor 2
        return Math.pow(10d, ((double) txPower - rssi) / (10 * 4));
    }

    void recordON(){
        if(mActivity.hasWindowFocus() == true){
            if(rcamera == null) {
                rcamera = new RCamera(mContext,mActivity);
            }

            if(rcamera.getCameraState() == false){
                mActivity.invalidateOptionsMenu(); // Refrecamos el menú

                try {
                    rcamera.startRecording();
                }

                catch (IOException e) {
                    String message = e.getMessage();
                    Log.i(null, "Problem Start" + message);
                    rcamera.getMrec().release();
                }
            }
        }
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

                if (deviceFound == false && BTAdapter.isEnabled()) {
                    BTAdapter.cancelDiscovery();

                    mContext.stopService(new Intent(mContext, BService.class));

                    rssi_msg.setText("NO HAY PELIGRO.");
                    rssi_dist.setText("");
                }

                else if(deviceFound == false && !BTAdapter.isEnabled()){
                    BTAdapter.cancelDiscovery();

                    mContext.stopService(new Intent(mContext, BService.class));

                    rssi_msg.setText("Bluetooth desactivado.");
                    rssi_dist.setText("");

                    notifi.bluetooth_desactivado();
                }

                else {
                    deviceFound = false;
                }
            }
        }, DELAY);
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

    // Devuelve el objeto de la clase RCamera
    RCamera getRcamera(){
        return rcamera;
    }

    // Set para inicializar el objeto de la cámara
    void setRcamera(RCamera rc){
        rcamera = rc;
    }
}
