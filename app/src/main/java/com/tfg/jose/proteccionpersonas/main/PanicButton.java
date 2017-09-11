package com.tfg.jose.proteccionpersonas.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;

import com.tfg.jose.proteccionpersonas.R;

/**
 * Created by jose on 29/01/16.
 *
 *  Clase del botón de pánico. Botón que al pulsarlo llama a un número.
 */

public class PanicButton {

    private Context mContext;
    private Activity mActivity;

    private Button b;

    private String telefono;

    // Constructor
    public PanicButton(Context context, Activity activity){
        this.mContext = context;
        this.mActivity = activity;
        this.telefono = "tel:999999999";
    }

    // Función para poner el botón de pánico
    void pushButton(){
        b = (Button) mActivity.findViewById(R.id.panicButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse(telefono));
                mContext.startActivity(callIntent);
            }
        });
    }

}
