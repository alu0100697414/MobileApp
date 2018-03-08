package com.tfg.jose.proteccionpersonas.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tfg.jose.proteccionpersonas.R;
import com.tfg.jose.proteccionpersonas.webservices.Request;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jose on 29/01/16.
 *
 *  Clase del botón de pánico. Botón que al pulsarlo llama a un número.
 */

public class PanicButton {

    private Context mContext;
    private Activity mActivity;

    private DBase protectULLDB;

    private Button b;

    private String telefono;

    // Constructor
    public PanicButton(Context context, Activity activity){
        this.mContext = context;
        this.mActivity = activity;
        this.telefono = "tel:999999999";

        this.protectULLDB = new DBase(mContext);
    }

    // Función para poner el botón de pánico
    void pushButton(){
        b = (Button) mActivity.findViewById(R.id.panicButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Envía aviso de que ha sido pulsado el botón de pánico
                Map<String, String> data = new HashMap<String, String>();

                data.put("mac", BackgroundVideoRecorder.getWifiMacAddress());

                List<String> info_server = new ArrayList<String>();
                info_server = protectULLDB.recuperarINFO_SERVER("1");

                if(!info_server.isEmpty()){

                    try { Request.panicButtonPushed(data, info_server.get(0)); }

                    catch (IOException e) { e.printStackTrace(); }
                    catch (ClassNotFoundException e) { e.printStackTrace(); }
                    catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
                    catch (NoSuchProviderException e) { e.printStackTrace(); }
                    catch (InvalidKeyException e) { e.printStackTrace(); }
                } else {
                    Toast.makeText(mContext, "Configure los datos de acceso al servidor.", Toast.LENGTH_SHORT).show();
                }

                // Realiza llamada
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse(telefono));
                mContext.startActivity(callIntent);
            }
        });
    }

}
