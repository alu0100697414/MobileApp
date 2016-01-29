package com.tfg.jose.proteccionpersonas;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.NotificationCompat;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.Set;

/**
 * Created by jose on 29/01/16.
 */
public class BluetoothConnection {

    private Inicio inicio;
    private Notification notifi;

    private String nombre_dispositivo;
    private String direccion_dispositivo;
    private int distancia_limite;
    private Set<BluetoothDevice> pairedDevices;

    // El adaptador bluetooth tiene que ser final y por tanto inicializado aquí
    private final BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();

    // Constructor
    public BluetoothConnection(Inicio ini){

        this.inicio = ini;
        this.notifi = new Notification(ini);

        this.nombre_dispositivo = "jose-TravelMate-5742-0";
        this.direccion_dispositivo = "00:15:83:E4:D7:86";
        this.distancia_limite = 10;
    }

    // Se llama al receiver cada vez que encuentra un dispositivo nuevo
    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action)) {

                double px = -54; // Valor rssi a un metro de distancia
                double rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                double distance = getDistance(rssi, px);
                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);

                // Si encuentra el dispositivo del agresor/a entra:
                if(name.equals(getNombre_dispositivo())){

                    // Parseamos el resultado para que muestre dos decimales
                    DecimalFormat df = new DecimalFormat("#.##");
                    String rdistance = df.format(distance);

                    TextView rssi_msg = (TextView) inicio.findViewById(R.id.res_busqueda);
                    TextView res_dist = (TextView) inicio.findViewById(R.id.res_distancia);

                    // Si está dentro de la distancia límite se le avisa
                    if(distance < getDistancia_limite()){
                        rssi_msg.setText("¡PELIGRO!\nSe ha superado la distancia límite. El agresor se encuentra a una distancia aproximada de:");
                        res_dist.setText(rdistance + "m");

                        notifi.vibrar();
                        notifi.notificar();
                    }

                    // Si lo encuentra pero no la supera, se le dice
                    else {
                        rssi_msg.setText("Fuera de la distancia de peligro.");
                        res_dist.setText(rdistance + "m");
                    }

                    // Finalizamos la búsqueda si lo encontramos
                    Toast.makeText(inicio, "Búsqueda finalizada.", Toast.LENGTH_SHORT).show();
                    BTAdapter.cancelDiscovery();

                }

            }
        }
    };

    // Devuelve la distancia aproximada en metros entre dos dispositivos
    double getDistance(double rssi, double txPower) {
        // El 4 es el valor de n y si no hay obstáculos de por medio se usa el valor 2
        return Math.pow(10d, ((double) txPower - rssi) / (10 * 4));
    }

    // Si está desactivado el Bluetooth, enviamos mensaje para activarlo
    void estaActivado(){
        if (!BTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            inicio.startActivityForResult(enableBtIntent, 1);
        }
    }

    // Coge dispositivos emparejados del telefono
    void emparejados(){
        // Coge los dispositivos emparejados
        pairedDevices = BTAdapter.getBondedDevices();
    }

    // Devuelve datos del dispositivo del agresor emparejado
    void emparejadoInfo(){
        if (pairedDevices.size() > 0) {

            String[] mArrayAdapter = new String[1];
            int i = 0;

            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {

                if(device.getName().equals(getNombre_dispositivo())) {
                    // Añadimos el nombre y la dirección para mostrarlo luego por el listview
                    mArrayAdapter[i] = ("Nombre: " + device.getName() + "\n" + "Dirección: " + device.getAddress());
                    i++;
                }
            }

            // Listamos todos los dispositivos emparejados con el nuestro
            ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(inicio, android.R.layout.simple_list_item_1, mArrayAdapter);

            ListView listView = (ListView) inicio.findViewById(R.id.list_view);
            listView.setAdapter(itemsAdapter);
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
        int DELAY = 30000;

        // Si cuando se acaba la búsqueda, no lo encontró, no hay peligro
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                TextView rssi_msg = (TextView) inicio.findViewById(R.id.res_busqueda);

                if (rssi_msg.getText().equals("Buscando...")) {
                    BTAdapter.cancelDiscovery();

                    rssi_msg.setText("NO HAY PELIGRO");
                    Toast.makeText(inicio, "Búsqueda finalizada.", Toast.LENGTH_SHORT).show();
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
}
