package com.tfg.jose.proteccionpersonas.main;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tfg.jose.proteccionpersonas.R;
import com.tfg.jose.proteccionpersonas.gps.GPSTracker;

import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Inicio extends AppCompatActivity {

    private PanicButton pbutton;
    private Notification notifi;

    ScheduledExecutorService executor;

    private DBase protectULLDB;

    private BLEConnection bleConnection;

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    // Constructor
    public Inicio(){
        pbutton = new PanicButton(Inicio.this, this);
        notifi = new Notification(Inicio.this, this);

        executor = Executors.newScheduledThreadPool(1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setLogo(R.mipmap.ic_launcher);

        // Inicializamos la base de datos
        protectULLDB = new DBase(getApplicationContext());

        pbutton.pushButton(); // Creamos el botón de pánico en la Activity

        bleConnection = new BLEConnection(Inicio.this,this);
        bleConnection.estaActivado();

        GPSTracker gps;
        gps = new GPSTracker(this);

        if (!gps.canGetLocation()){

            // Si no está activado, se envía aviso para activarlo
            AlertDialog.Builder bt_dialog = new AlertDialog.Builder(this);
            bt_dialog.setTitle("Activar GPS");
            bt_dialog.setMessage("Por favor, active el servicio GPS por si es necesario enviar su ubicación.");
            bt_dialog.setCancelable(false);
            bt_dialog.setPositiveButton("Activar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent toGPSEnable = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(toGPSEnable);
                }
            });
            bt_dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            bt_dialog.show();}

        gps.stopUsingGPS();
        gps.onDestroy();

        // Ejecutamos el servicio de busqueda de dispositivos bluetooth cada x tiempo
        Runnable searchB = new Runnable() {
            public void run() {

                if(bleConnection.isDiscovering()){
                    bleConnection.stopScanBLEDevices();
                }

                bleConnection.sinPeligro();
                bleConnection.startScanBLEDevices();
            }
        };

        executor.scheduleAtFixedRate(searchB, 0, 10, TimeUnit.SECONDS);
    }


    // Si se pulsa el botón de atrás, sigue ejecutándose la app
    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_inicio, menu);

        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {

        // Mostramos el botón de activar el bluetooth si no está activado.
        MenuItem bt = menu.findItem(R.id.bluetooth);
        if(bleConnection.bleState()) {
            bt.setVisible(false);
        } else {
            bt.setVisible(true);
        }

        // Cambiamos el título del botón de la cámara dependiendo de su estado.
        MenuItem video = menu.findItem(R.id.video);
        if(bleConnection.isMyServiceRunning(BackgroundVideoRecorder.class) == true) {
            video.setTitle(R.string.parar_grabacion);
        } else {
            video.setTitle(R.string.iniciar_grabacion);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_contactos) {
            startActivity(new Intent(Inicio.this,ContactList.class));
        }


        if(id == R.id.video){
            if(bleConnection.isMyServiceRunning(BackgroundVideoRecorder.class) == true){
                stopService(new Intent(this, BackgroundVideoRecorder.class));
                invalidateOptionsMenu(); // Refrecamos el menú

                TextView grabando = (TextView) findViewById(R.id.grabando);
                grabando.setCompoundDrawablesWithIntrinsicBounds(R.drawable.grabando, 0, 0, 0);
                grabando.setVisibility(View.INVISIBLE);
            }
            else {
                startService(new Intent(this, BackgroundVideoRecorder.class));
                invalidateOptionsMenu(); // Refrecamos el menú

                TextView grabando = (TextView) findViewById(R.id.grabando);
                grabando.setCompoundDrawablesWithIntrinsicBounds(R.drawable.grabando, 0, 0, 0);
                grabando.setVisibility(View.VISIBLE);
            }
        }

        // Activa el bluetooth
        if(id == R.id.bluetooth){
            bleConnection.estaActivado();
        }

        // Sale pestaá para actualizar la info del usuario
        if(id == R.id.action_info_usuario){

            LayoutInflater factory = LayoutInflater.from(Inicio.this);

            final View textEntryView = factory.inflate(R.layout.add_contact_dialog, null);

            List<Contact> contacto = new ArrayList<Contact>();
            contacto = protectULLDB.recuperarINFO_USUARIO();

            final List<Contact> contactoF = contacto;

            final EditText input1 = (EditText) textEntryView.findViewById(R.id.contact_name);
            final EditText input2 = (EditText) textEntryView.findViewById(R.id.contact_phone);

            if(!contacto.isEmpty()){

                Contact con = new Contact(contacto.get(0).getName(),contacto.get(0).getNumber(),1);

                input1.setText(con.getName());
                input2.setText(con.getNumber());
            }

            final AlertDialog.Builder alert = new AlertDialog.Builder(Inicio.this);
                    alert.setTitle(getString(R.string.update_title))
                         .setMessage(getString(R.string.update_text))
                         .setView(textEntryView)
                         .setPositiveButton(getString(R.string.actualizar),
                                 new DialogInterface.OnClickListener() {
                                     public void onClick(DialogInterface dialog, int whichButton) {
                                         if (!contactoF.isEmpty()) {
                                             protectULLDB.modificarINFO_USUARIO(input2.getText().toString(), input1.getText().toString());
                                         } else {
                                             protectULLDB.insertarINFO_USUARIO(input2.getText().toString(), input1.getText().toString());
                                         }

                                         Snackbar.make(getWindow().getDecorView().getRootView(), getString(R.string.datos_actualizados), Snackbar.LENGTH_LONG).show();
                                     }
                                 })
                            .setNegativeButton(getString(R.string.cancelar),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                             /*
                                             * User clicked cancel so do some stuff
                                             */
                            }
                         });
            alert.show();
        }

        // Sale pestaá para actualizar la info del usuario
        if(id == R.id.action_password){

            LayoutInflater factory = LayoutInflater.from(Inicio.this);

            final View textEntryView = factory.inflate(R.layout.password_menu, null);

            final EditText password = (EditText) textEntryView.findViewById(R.id.password_user);
            final EditText confirm_password = (EditText) textEntryView.findViewById(R.id.password_user_confirm);

            final Context context = this;

            final View vista = this.findViewById(android.R.id.content);

            final AlertDialog.Builder alert = new AlertDialog.Builder(Inicio.this);
            alert.setTitle(getString(R.string.update_pass_title))
                    .setMessage(getString(R.string.update_pass_text))
                    .setView(textEntryView)
                    .setPositiveButton(getString(R.string.actualizar),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    if (password.getText().toString().equals(confirm_password.getText().toString())) {
                                        protectULLDB.modificarCONFIG_APP("1", password.getText().toString());
                                        Snackbar.make(vista, getString(R.string.updated_pass), Snackbar.LENGTH_LONG).show();
                                    } else {
                                        Snackbar.make(vista, getString(R.string.updated_pass_error), Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            })
                    .setNegativeButton(getString(R.string.cancelar),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                             /*
                                             * User clicked cancel so do some stuff
                                             */
                                }
                            });
            alert.show();
        }

        if (id == R.id.action_server) {

            LayoutInflater factory = LayoutInflater.from(Inicio.this);

            final View textEntryView = factory.inflate(R.layout.server_layout, null);

            List<String> info_server = new ArrayList<String>();
            info_server = protectULLDB.recuperarINFO_SERVER("1");

            final EditText url_servidor = (EditText) textEntryView.findViewById(R.id.url_servidor);
            final EditText url_streaming = (EditText) textEntryView.findViewById(R.id.url_streaming);
            final EditText user_server = (EditText) textEntryView.findViewById(R.id.user_server);
            final EditText pass_server = (EditText) textEntryView.findViewById(R.id.pass_server);
            final EditText short_streaming_url = (EditText) textEntryView.findViewById(R.id.short_streaming_url);

            if(!info_server.isEmpty()){
                Log.i("VARSBD",info_server.get(0));
                Log.i("VARSBD",info_server.get(1));
                Log.i("VARSBD",info_server.get(2));
                Log.i("VARSBD",info_server.get(3));
                Log.i("VARSBD",info_server.get(4));

                url_servidor.setText(info_server.get(0));
                url_streaming.setText(info_server.get(1));
                user_server.setText(info_server.get(2));
                pass_server.setText(info_server.get(3));
                short_streaming_url.setText(info_server.get(4));
            }

            final Context context = this;

            final View vista = this.findViewById(android.R.id.content);

            final AlertDialog.Builder alert = new AlertDialog.Builder(Inicio.this);
            alert.setTitle("Configurar acceso servidor")
                    .setView(textEntryView)
                    .setPositiveButton(getString(R.string.actualizar),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                    protectULLDB.modificarINFO_SERVER("1",url_servidor.getText().toString(),url_streaming.getText().toString(),user_server.getText().toString(),pass_server.getText().toString(),short_streaming_url.getText().toString());
                                    Snackbar.make(vista, "Información del servidor actualizada.", Snackbar.LENGTH_LONG).show();



//                                    if (password.getText().toString().equals(confirm_password.getText().toString())) {
//                                        protectULLDB.modificarCONFIG_APP("1", password.getText().toString());
//                                        Snackbar.make(vista, getString(R.string.updated_pass), Snackbar.LENGTH_LONG).show();
//                                    } else {
//                                        Snackbar.make(vista, getString(R.string.updated_pass_error), Snackbar.LENGTH_LONG).show();
//                                    }
                                }
                            })
                    .setNegativeButton(getString(R.string.cancelar),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                }
                            });
            alert.show();

        }

        // Botón para cerrar la app.
        if(id == R.id.action_exit){
            // Muestro alerta para confirmación.
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.aviso))
                    .setMessage(getString(R.string.cerrar_app_text))
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                            System.exit(0); // Salimos si acepta el dialogo
                        }
                    }).create().show();
        }

        return super.onOptionsItemSelected(item);
    }

    // Callback para cuando se devuelve algun estado
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // Si activo el Bluetooth
        if (resultCode == -1) {
            System.exit(0); // Reiniciamos la API para que no haya problema a la hora de buscar dispositivos
            this.startActivity(new Intent(this.getApplicationContext(), Inicio.class));

            invalidateOptionsMenu(); // Refrescamos el menú

            Toast.makeText(Inicio.this, R.string.b_activado, Toast.LENGTH_SHORT).show();
        }

        // Si no lo activó
        else if (resultCode == 0){
            TextView rssi_msg = (TextView) this.findViewById(R.id.res_busqueda);
            rssi_msg.setText(R.string.b_desactivado);

            TextView rssi_dist = (TextView) this.findViewById(R.id.res_distancia);
            rssi_dist.setText("");

            invalidateOptionsMenu(); // Refrecamos el menú

            notifi.bluetooth_desactivado();
        }
    }

    // Destructor para cuando se cierre el programa
    @Override
    public void onDestroy() {
        super.onDestroy();

        if(bleConnection.isDiscovering()){
            bleConnection.stopScanBLEDevices();
        }

        if(!executor.isShutdown()){
            executor.shutdown();
        }
    }
}
