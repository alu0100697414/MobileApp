package com.tfg.jose.proteccionpersonas;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;

/**
 * Created by jose on 29/01/16.
 */
public class PanicButton {

    private Inicio inicio;
    private Button b;

    private String telefono;

    public PanicButton(Inicio ini){
        this.inicio = ini;
        this.telefono = "tel:689316443";
    }

    // Función para poner el botón de pánico
    void pushButton(){
        b = (Button) inicio.findViewById(R.id.panicButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse(telefono));
                inicio.startActivity(callIntent);
            }
        });
    }

}
