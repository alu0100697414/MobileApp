package com.tfg.jose.proteccionpersonas.webservices;

import android.text.format.Time;
import android.util.Log;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.tfg.jose.proteccionpersonas.encrypt.AESUtil;
import com.tfg.jose.proteccionpersonas.encrypt.KeysReader;

import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;

/**
 * Created by Jose Angel.
 */

public class Request {

    //Función que registra a un usuario en el servicio web la primera vez que usa la app
    public static void newUser(String MAC) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {

        // Generamos la clave secreta con la que cifrará posteriormente el AES
        String key = KeysReader.generarClaveCompartida(KeysReader.getPrivKeyClient(), KeysReader.getPubKeyServer());

        // Ciframos los parametros que le enviamos al servidor web
        String CMac = AESUtil.encrypt(MAC,key);
        String CServerLink = AESUtil.encrypt(StreamingConfig.STREAM_SHORT_URL,key);

        // Creamos un hash con todas las variables que vamos a enviar
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("name", CMac);
        params.put("server", CServerLink);

        // Creamos el JSON y lo añadimos a la cola
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST, Config.SERVER_URL + "/camara", new JSONObject(params),
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("Volley newUser Request ", response.toString());
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("Volley newU Req Error ", error.toString());
                    }
                });

        Config.requestQueue.add(jsonObjectRequest);
    }


    //Función que establece un video como online
    public static void streamOnline(String MAC, String name, String tlf, String lat, String lon) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {

        // Cogemos la fecha y hora actuales
        Time now = new Time(Time.getCurrentTimezone());
        now.setToNow();

        String minute;
        String hour;

        int min = now.minute; if(min < 10){minute = "0" + String.valueOf(min);} else { minute = String.valueOf(min); }
        int hou = now.hour; if(hou < 10){hour = "0" + String.valueOf(hou);} else { hour = String.valueOf(hou); }

        String fecha = now.monthDay + "/" + (now.month+1) + "/" + now.year + " - " + hour + ":" + minute;

        // Generamos la clave secreta con la que cifrará posteriormente el AES
        String key = KeysReader.generarClaveCompartida(KeysReader.getPrivKeyClient(), KeysReader.getPubKeyServer());

        // Ciframos los parametros que le enviamos al servidor web
        String CServer = AESUtil.encrypt(StreamingConfig.STREAM_SHORT_URL,key);
        String CNombre = AESUtil.encrypt(name,key);
        String CNumero = AESUtil.encrypt(tlf,key);
        String CTime_now = AESUtil.encrypt(fecha,key);
        String CLat = AESUtil.encrypt(lat,key);
        String CLong = AESUtil.encrypt(lon,key);

        // Creamos un hash con todas las variables que vamos a enviar
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("name", MAC);
        params.put("server", CServer);
        params.put("nombre", CNombre);
        params.put("numero", CNumero);
        params.put("time_now", CTime_now);
        params.put("latitude", CLat);
        params.put("longitude", CLong);

        // Creamos el JSON y lo añadimos a la cola
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.PUT, Config.SERVER_URL + "/online/" + MAC, new JSONObject(params),
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("Volley SrtOn Request ", response.toString());
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("Volley SrtOn Req Error ", error.toString());
                    }
                });

        Config.requestQueue.add(jsonObjectRequest);
    }


    //Función que establece un video como offline
    public static void streamOffline(String MAC) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {

        // Cogemos la fecha y hora actuales
        Time now = new Time(Time.getCurrentTimezone());
        now.setToNow();

        String minute;
        String hour;

        int min = now.minute; if(min < 10){minute = "0" + String.valueOf(min);} else { minute = String.valueOf(min); }
        int hou = now.hour; if(hou < 10){hour = "0" + String.valueOf(hou);} else { hour = String.valueOf(hou); }

        String fecha = now.monthDay + "/" + (now.month+1) + "/" + now.year + " - " + hour + ":" + minute;

        // Generamos la clave secreta con la que cifrará posteriormente el AES
        String key = KeysReader.generarClaveCompartida(KeysReader.getPrivKeyClient(), KeysReader.getPubKeyServer());

        // Ciframos los parametros que le enviamos al servidor web
        String CFecha = AESUtil.encrypt(fecha,key);

        // Creamos un hash con todas las variables que vamos a enviar
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("date_last_online", CFecha);

        // Creamos el JSON y lo añadimos a la cola
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.PUT, Config.SERVER_URL + "/offline/" + MAC, new JSONObject(params),
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("Volley SrtOff Request ", response.toString());
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("Volley SrtOff Req Err ", error.toString());
                    }
                });

        Config.requestQueue.add(jsonObjectRequest);
    }
}