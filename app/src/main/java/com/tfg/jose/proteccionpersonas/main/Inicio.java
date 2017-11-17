package com.tfg.jose.proteccionpersonas.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
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

import com.android.volley.toolbox.Volley;
import com.tfg.jose.proteccionpersonas.R;
import com.tfg.jose.proteccionpersonas.gps.GPSTracker;
import com.tfg.jose.proteccionpersonas.webservices.Config;
import com.tfg.jose.proteccionpersonas.webservices.Request;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Inicio extends AppCompatActivity {

    private PanicButton pbutton;
    private Notification mNotification;
    private DBase protectULLDB;
    private BLEConnection bleConnection;
    private GPSTracker gps;

    ScheduledExecutorService executor;

    Handler handler = new Handler();

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    // Constructor
    public Inicio(){
        pbutton = new PanicButton(Inicio.this, this);
        mNotification = new Notification(Inicio.this, this);

        executor = Executors.newScheduledThreadPool(1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setLogo(R.mipmap.ic_launcher);

        IntentFilter filter = new IntentFilter(Intent.ACTION_SHUTDOWN);
        BroadcastReceiver mReceiver = new ShutDownReceiver();
        registerReceiver(mReceiver, filter);

        // Inicializamos la base de datos
        protectULLDB = new DBase(getApplicationContext());

        Config.requestQueue = Volley.newRequestQueue(this);

        pbutton.pushButton(); // Creamos el botón de pánico en la Activity

        bleConnection = new BLEConnection(Inicio.this, this);
        bleConnection.isActivated();

        gps = new GPSTracker(this);

        if (!gps.canGetLocation()) {
            // Si no está activado, se envía aviso para activarlo
            AlertDialog.Builder bt_dialog = new AlertDialog.Builder(this);
            bt_dialog.setTitle("Activar GPS");
            bt_dialog.setMessage("Por favor, active el servicio GPS por si es necesario enviar su ubicación.");
            bt_dialog.setCancelable(false);
            bt_dialog.setPositiveButton("Activar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent toGPSEnable = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(toGPSEnable, 123);
                }
            });
            bt_dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mNotification.gps_desactivado();

                    // Enviamos incidencia al servidor
                    Map<String, String> data = new HashMap<String, String>();

                    data.put("mac", BackgroundVideoRecorder.getWifiMacAddress());

                    List<Contact> contacto;
                    contacto = protectULLDB.recuperarINFO_USUARIO();

                    if (!contacto.isEmpty()) {
                        data.put("name", contacto.get(0).getName());
                        data.put("number", contacto.get(0).getNumber());
                    } else {
                        data.put("name", getString(R.string.no_definido));
                        data.put("number", getString(R.string.no_definido));
                    }

                    data.put("type_incidence", "4");
                    data.put("text_incidence", "GPS de la víctima desactivado.");

                    List<String> info_server = new ArrayList<String>();
                    info_server = protectULLDB.recuperarINFO_SERVER("1");

                    if(!info_server.isEmpty()){

                        try {
                            Request.sendIncidence(data, info_server.get(0));
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (NoSuchProviderException e) {
                            e.printStackTrace();
                        } catch (InvalidKeyException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Configure los datos de acceso al servidor.", Toast.LENGTH_SHORT).show();
                        sendPingToServer(10);
                    }
                }
            });
            bt_dialog.show();
        }


        // Ejecutamos el servicio de busqueda de dispositivos bluetooth cada x tiempo
        Runnable searchBleDevice = new Runnable() {
            public void run() {
                if (bleConnection.isDiscovering()) {
                    bleConnection.stopScanBLEDevices();
                }

                bleConnection.withoutDanger();
                bleConnection.startScanBLEDevices();
            }
        };

        executor.scheduleAtFixedRate(searchBleDevice, 0, 15, TimeUnit.SECONDS);

        // Enviamos ping al servidor, la primera vez a los 10 segundos
        sendPingToServer(10);
    }


    public void sendPingToServer(int timeToNextPing) {

        final Inicio inicio = this;
        final Activity activity = this;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Map<String, String> data = new HashMap<String, String>();

                data.put("mac", BackgroundVideoRecorder.getWifiMacAddress());

                List<Contact> contacto;
                contacto = protectULLDB.recuperarINFO_USUARIO();

                if(!contacto.isEmpty()){
                    data.put("name", contacto.get(0).getName());
                    data.put("number", contacto.get(0).getNumber());
                } else {
                    data.put("name", getString(R.string.no_definido));
                    data.put("number", getString(R.string.no_definido));
                }

                gps = new GPSTracker(activity.getApplicationContext());

                if (gps.canGetLocation() && gps != null){
                    data.put("latitude", String.valueOf(gps.getLocation().getLatitude()));
                    data.put("longitude", String.valueOf(gps.getLocation().getLongitude()));
                } else {
                    data.put("latitude", "null");
                    data.put("longitude", "null");
                    mNotification.gps_error();
                }

                BatteryManager bm = (BatteryManager)getSystemService(BATTERY_SERVICE);
                int batLevel = 0;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                }

                data.put("battery", String.valueOf(batLevel));

                List<String> info_server = new ArrayList<String>();
                info_server = protectULLDB.recuperarINFO_SERVER("1");

                if(!info_server.isEmpty()){

                    try { Request.pingStatusDevice(data, info_server.get(0), inicio, activity, getApplicationContext()); }

                    catch (IOException e) { e.printStackTrace(); }
                    catch (ClassNotFoundException e) { e.printStackTrace(); }
                    catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
                    catch (NoSuchProviderException e) { e.printStackTrace(); }
                    catch (InvalidKeyException e) { e.printStackTrace(); }
                } else {
                    Toast.makeText(activity, "Configure los datos de acceso al servidor.", Toast.LENGTH_SHORT).show();
                    sendPingToServer(10);
                }

            }
        }, TimeUnit.SECONDS.toMillis(timeToNextPing));
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
            bleConnection.isActivated();
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
                                public void onClick(DialogInterface dialog, int whichButton) {}
                 }
            );
            alert.show();
        }

        // Sale pestaá para actualizar la info del usuario
        if(id == R.id.action_password){
            LayoutInflater factory = LayoutInflater.from(Inicio.this);
            final View textEntryView = factory.inflate(R.layout.password_menu, null);

            final EditText password = (EditText) textEntryView.findViewById(R.id.password_user);
            final EditText confirm_password = (EditText) textEntryView.findViewById(R.id.password_user_confirm);

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
                                public void onClick(DialogInterface dialog, int whichButton) {}
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
                url_servidor.setText(info_server.get(0));
                url_streaming.setText(info_server.get(1));
                user_server.setText(info_server.get(2));
                pass_server.setText(info_server.get(3));
                short_streaming_url.setText(info_server.get(4));
            }

            final View vista = this.findViewById(android.R.id.content);

            final AlertDialog.Builder alert = new AlertDialog.Builder(Inicio.this);
            final List<String> finalInfo_server = info_server;
            alert.setTitle("Configurar acceso servidor")
                    .setView(textEntryView)
                    .setPositiveButton(getString(R.string.actualizar),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    if(!finalInfo_server.isEmpty()) {
                                        protectULLDB.modificarINFO_SERVER("1", url_servidor.getText().toString(), url_streaming.getText().toString(), user_server.getText().toString(), pass_server.getText().toString(), short_streaming_url.getText().toString());
                                    } else {
                                        protectULLDB.insertarINFO_SERVER("1", url_servidor.getText().toString(), url_streaming.getText().toString(), user_server.getText().toString(), pass_server.getText().toString(), short_streaming_url.getText().toString());
                                    }
                                    Snackbar.make(vista, "Información del servidor actualizada.", Snackbar.LENGTH_LONG).show();
                                }
                            })
                    .setNegativeButton(getString(R.string.cancelar),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {}
                            });
            alert.show();

        }

        // Botón para cerrar la app.
        if(id == R.id.action_exit){
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.aviso))
                    .setMessage(getString(R.string.cerrar_app_text))
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {

                            if(!executor.isShutdown()){
                                executor.shutdown();
                            }

                            // Enviamos incidencia al servidor
                            Map<String, String> data = new HashMap<String, String>();

                            data.put("mac", BackgroundVideoRecorder.getWifiMacAddress());

                            List<Contact> contacto;
                            contacto = protectULLDB.recuperarINFO_USUARIO();

                            if(!contacto.isEmpty()){
                                data.put("name", contacto.get(0).getName());
                                data.put("number", contacto.get(0).getNumber());
                            } else {
                                data.put("name", getString(R.string.no_definido));
                                data.put("number", getString(R.string.no_definido));
                            }

                            data.put("type_incidence", "1");
                            data.put("text_incidence", "Se ha cerrado la app");

                            List<String> info_server = new ArrayList<String>();
                            info_server = protectULLDB.recuperarINFO_SERVER("1");

                            if(!info_server.isEmpty()){

                                try { Request.sendIncidence(data, info_server.get(0)); }

                                catch (IOException e) { e.printStackTrace(); }
                                catch (ClassNotFoundException e) { e.printStackTrace(); }
                                catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
                                catch (NoSuchProviderException e) { e.printStackTrace(); }
                                catch (InvalidKeyException e) { e.printStackTrace(); }
                            } else {
                                Toast.makeText(getApplicationContext(), "Configure los datos de acceso al servidor.", Toast.LENGTH_SHORT).show();
                                sendPingToServer(10);
                            }

                            // Cerramos aplicación
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

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // Si activo el Bluetooth
        if (requestCode == 1 && resultCode == -1) {
            System.exit(0); // Reiniciamos la API para que no haya problema a la hora de buscar dispositivos
            this.startActivity(new Intent(this.getApplicationContext(), Inicio.class));
            invalidateOptionsMenu(); // Refrescamos el menú
            Toast.makeText(Inicio.this, R.string.b_activado, Toast.LENGTH_SHORT).show();
        }

        // Si no lo activó
        else if (requestCode == 1 && resultCode == 0){
            TextView rssi_msg = (TextView) this.findViewById(R.id.res_busqueda);
            rssi_msg.setText(R.string.b_desactivado);

            TextView rssi_dist = (TextView) this.findViewById(R.id.res_distancia);
            rssi_dist.setText("");

            invalidateOptionsMenu(); // Refrecamos el menú

            mNotification.bluetooth_desactivado();
        }

        else if (requestCode == 123) { // GPS result

            gps = new GPSTracker(this);
            if (!gps.canGetLocation()) {

                // Send GPS disabled incidence
                Map<String, String> data = new HashMap<String, String>();

                data.put("mac", BackgroundVideoRecorder.getWifiMacAddress());

                List<Contact> contacto;
                contacto = protectULLDB.recuperarINFO_USUARIO();

                if(!contacto.isEmpty()){
                    data.put("name", contacto.get(0).getName());
                    data.put("number", contacto.get(0).getNumber());
                } else {
                    data.put("name", getString(R.string.no_definido));
                    data.put("number", getString(R.string.no_definido));
                }

                data.put("type_incidence", "4");
                data.put("text_incidence", "GPS de la víctima desactivado.");

                List<String> info_server = new ArrayList<String>();
                info_server = protectULLDB.recuperarINFO_SERVER("1");

                if(!info_server.isEmpty()){

                    try { Request.sendIncidence(data, info_server.get(0)); }

                    catch (IOException e) { e.printStackTrace(); }
                    catch (ClassNotFoundException e) { e.printStackTrace(); }
                    catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
                    catch (NoSuchProviderException e) { e.printStackTrace(); }
                    catch (InvalidKeyException e) { e.printStackTrace(); }
                } else {
                    Toast.makeText(getApplicationContext(), "Configure los datos de acceso al servidor.", Toast.LENGTH_SHORT).show();
                    sendPingToServer(10);
                }

            } else {
                // VER POR QUÉ NO FUNCIONA EL GPS AL REINICIAR LA ACTIVIDAD TRAS ACTIVARLO!!!!!!!
                Intent intent_test = getIntent();
                finish();
                startActivity(intent_test);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        handler.removeCallbacksAndMessages(null);

        if(bleConnection.isDiscovering()){
            bleConnection.stopScanBLEDevices();
        }
    }


    public class ShutDownReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
                Map<String, String> data = new HashMap<String, String>();

                data.put("mac", BackgroundVideoRecorder.getWifiMacAddress());

                List<Contact> contacto;
                contacto = protectULLDB.recuperarINFO_USUARIO();

                if(!contacto.isEmpty()){
                    data.put("name", contacto.get(0).getName());
                    data.put("number", contacto.get(0).getNumber());
                } else {
                    data.put("name", getString(R.string.no_definido));
                    data.put("number", getString(R.string.no_definido));
                }

                data.put("type_incidence", "0");
                data.put("text_incidence", "El móvil se ha apagado");

                List<String> info_server = new ArrayList<String>();
                info_server = protectULLDB.recuperarINFO_SERVER("1");

                if(!info_server.isEmpty()){

                    try { Request.sendIncidence(data, info_server.get(0)); }

                    catch (IOException e) { e.printStackTrace(); }
                    catch (ClassNotFoundException e) { e.printStackTrace(); }
                    catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
                    catch (NoSuchProviderException e) { e.printStackTrace(); }
                    catch (InvalidKeyException e) { e.printStackTrace(); }
                } else {
                    Toast.makeText(getApplicationContext(), "Configure los datos de acceso al servidor.", Toast.LENGTH_SHORT).show();
                    sendPingToServer(10);
                }
            }
        }
    }

}
