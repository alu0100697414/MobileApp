package com.tfg.jose.proteccionpersonas;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Inicio extends AppCompatActivity {

    private BluetoothConnection bluetooth;
    private PanicButton pbutton;

    ScheduledExecutorService executor;

    // Constructor
    public Inicio(){
        bluetooth = new BluetoothConnection(Inicio.this, this);
        pbutton = new PanicButton(Inicio.this, this);

        executor = Executors.newScheduledThreadPool(1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Filtro para cuando encuentre dispositivos bluetooth
        IntentFilter bluetoothFilter = new IntentFilter();
        bluetoothFilter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetooth.getReceiver(), bluetoothFilter);

        pbutton.pushButton(); // Creamos el botón de pánico en la Activity

        bluetooth.estaActivado(); // Comprobamos si esta activado el bluetooth y sino, envia mensaje de activacion

        // Ejecutamos el servicio de busqueda de dispositivos bluetooth cada x tiempo
        Runnable searchB = new Runnable() {
            public void run() {
                startService(new Intent(Inicio.this, BService.class));

                bluetooth.sinPeligro(); // Si tras 12s no lo encuentra, muestra mensaje de que no hay peligro
            }
        };

        executor.scheduleAtFixedRate(searchB, 0, 15, TimeUnit.SECONDS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_inicio, menu);

        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {

        // Mostramos el botón de parar grabación en el menú solo si está grabando
        MenuItem stop = menu.findItem(R.id.stop_video);
        if(bluetooth.getRcamera() != null && bluetooth.getRcamera().getCameraState() == true) {
            stop.setVisible(true);
        } else {
            stop.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if(id == R.id.stop_video){
            if(bluetooth.getRcamera().getCameraState() == true){
                bluetooth.getRcamera().stopRecording(); // Para de grabar
            }
            else {
                Toast.makeText(Inicio.this, "No se está grabando.", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    // Callback para cuando se devuelve algun estado
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // Si activo el Bluetooth
        if (resultCode == -1) {
            System.exit(0); // Reiniciamos la API para que no haya problema a la hora de buscar dispositivos
            this.startActivity(new Intent(this.getApplicationContext(), Inicio.class));

            Toast.makeText(Inicio.this, "Ha activado el Bluetooth.", Toast.LENGTH_SHORT).show();
        }

        // Si no lo activó
        else if (resultCode == 0){
            TextView rssi_msg = (TextView) this.findViewById(R.id.res_busqueda);
            rssi_msg.setText("Bluetooth desactivado.");

            Toast.makeText(Inicio.this, "No ha activado el Bluetooth.", Toast.LENGTH_SHORT).show();
        }
    }

    // Devuelve el objeto de la clase Bluetooth Connection
    BluetoothConnection getBluetoothConnection(){
        return bluetooth;
    }

    // Destructor para cuando se cierre el programa
    @Override
    public void onDestroy() {
        super.onDestroy();

        if(bluetooth.getRcamera() != null){

            if(bluetooth.getRcamera().getCameraState() == true) {
                bluetooth.getRcamera().stopRecording();
            }

            bluetooth.getRcamera().stopCamera();
        }

        bluetooth.estaBuscando(); // Vuelvo a comprobar que esté parada la busqueda de dispositivos
        unregisterReceiver(bluetooth.getReceiver());

        executor.shutdown();
    }
}
