package com.tfg.jose.proteccionpersonas;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Inicio extends AppCompatActivity {

    private BluetoothConnection bluetooth;
    private PanicButton pbutton;
    private Notification notifi;

    ScheduledExecutorService executor;

    private DBase protectULLDB;

    // Constructor
    public Inicio(){
        bluetooth = new BluetoothConnection(Inicio.this, this);
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

        // Inicializamos la base de datos
        protectULLDB = new DBase(getApplicationContext());

        // Filtro para cuando encuentre dispositivos bluetooth
        IntentFilter bluetoothFilter = new IntentFilter();
        bluetoothFilter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetooth.getReceiver(), bluetoothFilter);

        pbutton.pushButton(); // Creamos el botón de pánico en la Activity

        bluetooth.estaActivado();

        // Ejecutamos el servicio de busqueda de dispositivos bluetooth cada x tiempo
        Runnable searchB = new Runnable() {
            public void run() {

                startService(new Intent(Inicio.this, BService.class));

                bluetooth.sinPeligro(); // Si tras 12s no lo encuentra, muestra mensaje de que no hay peligro

                invalidateOptionsMenu(); // Refrescamos el menu
            }
        };

        executor.scheduleAtFixedRate(searchB, 0, 15, TimeUnit.SECONDS);
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
        if(bluetooth.getBTAdapter().isEnabled()) {
            bt.setVisible(false);
        } else {
            bt.setVisible(true);
        }

        // Cambiamos el título del botón de la cámara dependiendo de su estado.
        MenuItem video = menu.findItem(R.id.video);
        if(bluetooth.isMyServiceRunning(BackgroundVideoRecorder.class) == true) {
            video.setTitle("Parar grabación");
        } else {
            video.setTitle("Iniciar grabación");
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
            if(bluetooth.isMyServiceRunning(BackgroundVideoRecorder.class) == true){
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
            bluetooth.estaActivado();
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
                    alert.setTitle("Actualice su información personal")
                         .setMessage("Mantenga su información de contacto actualizada por seguridad.")
                         .setView(textEntryView)
                         .setPositiveButton("ACTUALIZAR",
                                 new DialogInterface.OnClickListener() {
                                     public void onClick(DialogInterface dialog, int whichButton) {
                                         if (!contactoF.isEmpty()) {
                                             protectULLDB.modificarINFO_USUARIO(input2.getText().toString(), input1.getText().toString());
                                         } else {
                                             protectULLDB.insertarINFO_USUARIO(input2.getText().toString(), input1.getText().toString());
                                         }

                                         Snackbar.make(getWindow().getDecorView().getRootView(), "Datos actualizados.", Snackbar.LENGTH_LONG).show();
                                     }
                                 })
                            .setNegativeButton("CANCELAR",
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
            alert.setTitle("Actualizar contraseña")
                    .setMessage("Tenga actualizada la contraseña para evitar que puedan entrar en la aplicación.")
                    .setView(textEntryView)
                    .setPositiveButton("ACTUALIZAR",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    if (password.getText().toString().equals(confirm_password.getText().toString())) {
                                        protectULLDB.modificarCONFIG_APP("1", password.getText().toString());
                                        Snackbar.make(vista, "Contraseña actualizada.", Snackbar.LENGTH_LONG).show();
                                    } else {
                                        Snackbar.make(vista, "Las contraseñas no coinciden.", Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            })
                    .setNegativeButton("CANCELAR",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                             /*
                                             * User clicked cancel so do some stuff
                                             */
                                }
                            });
            alert.show();
        }

        // Botón para cerrar la app.
        if(id == R.id.action_exit){
            // Muestro alerta para confirmación.
            new AlertDialog.Builder(this)
                    .setTitle("¡AVISO!")
                    .setMessage("Si cierra la app, quedará desprotegid@.")
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

            Toast.makeText(Inicio.this, "Ha activado el Bluetooth.", Toast.LENGTH_SHORT).show();
        }

        // Si no lo activó
        else if (resultCode == 0){
            TextView rssi_msg = (TextView) this.findViewById(R.id.res_busqueda);
            rssi_msg.setText("Bluetooth desactivado.");

            TextView rssi_dist = (TextView) this.findViewById(R.id.res_distancia);
            rssi_dist.setText("");

            invalidateOptionsMenu(); // Refrecamos el menú

            notifi.bluetooth_desactivado();
        }
    }

    // Devuelve el objeto de la clase Bluetooth Connection
    BluetoothConnection getBluetoothConnection(){
        return bluetooth;
    }

    // Devuelve el objeto de las notificaciones
    Notification getNotifi(){
        return notifi;
    }

    // Destructor para cuando se cierre el programa
    @Override
    public void onDestroy() {
        super.onDestroy();

        bluetooth.estaBuscando(); // Vuelvo a comprobar que esté parada la busqueda de dispositivos
        unregisterReceiver(bluetooth.getReceiver());


        if(!executor.isShutdown()){
            executor.shutdown();
        }
    }
}
