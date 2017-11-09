package com.tfg.jose.proteccionpersonas.main;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.tfg.jose.proteccionpersonas.R;

import java.text.DecimalFormat;

/**
 * Created by Jose on 22/10/2016.
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BLEConnection {

    private Context mContext;
    private Activity mActivity;

    private Notification mNotification;

    private boolean deviceFound; // True si encuentra el dispositivo
    private double px;  // Valor de intensidad de señal entre dos dispositivos a un metro de distancia.

    private final static int REQUEST_ENABLE_BT = 1;

    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;

    // Constructor
    public BLEConnection(Context context, Activity activity){
        this.mContext = context;
        this.mActivity = activity;

        this.mNotification = new Notification(context, activity);

        btManager = (BluetoothManager)activity.getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();

        this.deviceFound = false;
        this.px = -54;

        btAdapter.startLeScan(leScanCallback);
    }

    // Si está desactivado el Bluetooth, enviamos mensaje para activarlo
    void isActivated(){
        if (!btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        }
    }

    boolean bleState(){
        if(btAdapter.isEnabled()){ return true; }
        else { return false; }
    }

    void startScanBLEDevices(){
        btAdapter.startLeScan(leScanCallback);
    }

    void stopScanBLEDevices(){
        btAdapter.stopLeScan(leScanCallback);
    }

    boolean isDiscovering(){
        return btAdapter.isDiscovering();
    }

    // Devuelve la distancia aproximada en metros entre dos dispositivos
    double getDistance(double rssi, double txPower) {
        // El 2.7 es el valor de n y si no hay obstáculos de por medio se usa el valor 2
        return Math.pow(10d, ((double) txPower - rssi) / (10 * 2.7));
    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // Si no encuentra nada tras doce segundos
    void withoutDanger(){

        int DELAY = 12000; // En ms

        // Si cuando se acaba la búsqueda, no lo encontró, no hay peligro
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                TextView rssi_msg = (TextView) mActivity.findViewById(R.id.res_busqueda);
                TextView rssi_dist = (TextView) mActivity.findViewById(R.id.res_distancia);

                mActivity.invalidateOptionsMenu(); // Refrescamos el menu

                /*if (deviceFound == false && btAdapter.isEnabled()) {
                    btAdapter.stopLeScan(leScanCallback);

                    // Acutlizamos a 0 para si vuelve a encotnrar al agresor
                    mNotification.setSms_enviado(0);

                    rssi_msg.setText(mContext.getString(R.string.sin_peligro));
                    rssi_dist.setText("");

                } else */
                if (deviceFound == false && !btAdapter.isEnabled()) {
                    btAdapter.stopLeScan(leScanCallback);

//                    rssi_msg.setText(mContext.getString(R.string.b_desactivado));
//                    rssi_dist.setText("");

                    mNotification.bluetooth_desactivado();
                } else {
                    deviceFound = false;
                }
            }
        }, DELAY);
    }

    // Se llama cada vez que se detecta un dispositivo BLE
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {

            Log.i("FIND_D", "Encontrado: " + device.getName());
            Log.i("FIND_D", "Encontrado: " + device.getAddress());

            // F4:BE:76:06:43:75
            if(device.getAddress().equals("40:4E:36:04:CE:BC")){
                btAdapter.stopLeScan(leScanCallback);

                // Calculamos la distancia aproximada
                double distance = getDistance(rssi, px);
                Log.i("BLE_DIST", String.valueOf(distance) + " metros.");

                deviceFound = true; // Encontró el dispositivo

                // Parseamos el resultado para que muestre dos decimales
                DecimalFormat df = new DecimalFormat("#.##");
                String rdistance = df.format(distance);

                TextView rssi_msg = (TextView) mActivity.findViewById(R.id.res_busqueda);
                TextView res_dist = (TextView) mActivity.findViewById(R.id.res_distancia);

                // Si el agresor supera la distancia límite
                rssi_msg.setText("URGENTE" + "\n" + "El agresor está muy próximo a usted.");
                res_dist.setText(rdistance + "m");

                // Notificamos a la víctima
                mNotification.notificar_limite();

                // Notificamos a los contactos
                // Mirar error al enviar sms
                // (https://stackoverflow.com/questions/32742327/neither-user-10102-nor-current-process-has-android-permission-read-phone-state)
//                mNotification.enviar_sms();
//                mNotification.setSms_enviado(1);

                // Abrimos aplicación si está en segundo plano
                if(mActivity.hasWindowFocus() == false) {
                    Intent intento = new Intent(mContext, Inicio.class);
                    intento.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    mContext.startActivity(intento);
                }

                // Comenzamos el vídeo streaming automáticamente
                if(isMyServiceRunning(BackgroundVideoRecorder.class) == false){
                    mContext.startService(new Intent(mContext, BackgroundVideoRecorder.class));
                }

                TextView recording = (TextView) mActivity.findViewById(R.id.grabando);
                recording.setCompoundDrawablesWithIntrinsicBounds(R.drawable.grabando, 0, 0, 0);
                recording.setVisibility(View.VISIBLE);

                mActivity.invalidateOptionsMenu(); // Refrescamos el menú
            }
        }
    };

    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            // this will get called after the client initiates a BluetoothGatt.discoverServices() call
        }
    };

}
