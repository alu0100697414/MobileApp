package com.tfg.jose.proteccionpersonas.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.tfg.jose.proteccionpersonas.R;

public class Autentication extends AppCompatActivity {

    private DBase protectULLDB;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autentication);

        // Inicializamos la base de datos
        protectULLDB = new DBase(getApplicationContext());

        // Recuperamos la contraseña y si no tiene, creamos una por defecto
        password = protectULLDB.recuperarCONFIG_APP("1");

        if(password.equals("")){
            protectULLDB.insertarCONFIG_APP("1","123456");
        }

        // EditText de la contraseña
        final EditText input = (EditText) findViewById(R.id.pass_toenter);

        // Botón para confirmar la contraseña
        Button b = (Button) findViewById(R.id.button_acceso_app);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                // Si introduce la contraseña correctamente puede acceder a la aplicación.
                if(password.equals(input.getText().toString())){
                    input.setText("");
                    startActivity(new Intent(Autentication.this, Inicio.class));
                } else {
                    Snackbar.make(v, R.string.pass_incorrecta, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

}
