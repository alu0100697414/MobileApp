package com.tfg.jose.proteccionpersonas;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class BService extends Service {

    private Inicio inicio;

    // Constructor por defecto
    public BService() {
        inicio = new Inicio();
    }

    // Se ejecuta nada más ejecutarse el Servicio
    @Override
    public void onCreate() {}

    // Comienza el Servicio
    @Override
    public int onStartCommand(Intent intenc, int flags, int idArranque) {

        if(inicio.getBluetoothConnection().getBTAdapter().isEnabled()){
            inicio.getBluetoothConnection().estaBuscando(); // Si esta buscando, para la busqueda
            inicio.getBluetoothConnection().buscar(); // Inicia la busqueda
        }

        return START_STICKY;
    }

    // Destructor del Servicio
    @Override
    public void onDestroy() {}

    // Método encargado de enlazar con la Activity
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
