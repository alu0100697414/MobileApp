package com.tfg.jose.proteccionpersonas.webservices;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.format.Time;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.tfg.jose.proteccionpersonas.R;
import com.tfg.jose.proteccionpersonas.encrypt.AESUtil;
import com.tfg.jose.proteccionpersonas.encrypt.KeysReader;
import com.tfg.jose.proteccionpersonas.main.Inicio;
import com.tfg.jose.proteccionpersonas.main.Notification;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jose Angel.
 */

public class Request {

    // Función que envía posición de la víctima como ping
    public static void pingStatusDevice(Map<String, String> info, String server, final Inicio inicio, final Activity mActivity, final Context mContext) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        // Generamos la clave secreta con la que cifrará posteriormente el AES
        String key = KeysReader.generateSharedKey(KeysReader.getPrivKeyClient(), KeysReader.getPubKeyServer());

        // Ciframos los parametros que le enviamos al servidor web
        String CName = AESUtil.encrypt(info.get("name"),key);
        String CNumber = AESUtil.encrypt(info.get("number"),key);
        String CLatitude = AESUtil.encrypt(info.get("latitude"),key);
        String CLongitude = AESUtil.encrypt(info.get("longitude"),key);
        String CBattery = AESUtil.encrypt(info.get("battery"),key);

        // Creamos un hash con todas las variables que vamos a enviar
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("battery", CBattery);
        params.put("name", CName);
        params.put("number", CNumber);
        params.put("latitude", CLatitude);
        params.put("longitude", CLongitude);

        // Creamos el JSON y lo añadimos a la cola
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.PUT, server + "/statusdevice/" + info.get("mac"), new JSONObject(params),
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        double distance = -1;
                        int nextPing = 10;

                        try {
                            distance = Double.parseDouble(response.get("distancia").toString());
                            nextPing = Integer.parseInt(response.get("nextPing").toString());
                        }
                        catch (JSONException e) {  e.printStackTrace(); }

                        TextView rssi_msg = (TextView) mActivity.findViewById(R.id.res_busqueda);
                        TextView res_dist = (TextView) mActivity.findViewById(R.id.res_distancia);
                        TextView rssi_dist = (TextView) mActivity.findViewById(R.id.res_distancia);

                        Notification mNotification = new Notification(mContext, mActivity);

                        // Actualizamos la información dependiendo de la distancia a la que se encuentre el agresor
                        if(distance < 1 && distance > 0.5){ // WARNING
                            rssi_msg.setText(mActivity.getString(R.string.aviso) + "\n" + mActivity.getString(R.string.mensaje_aviso));
                            res_dist.setText((int) (distance*1000) + "m");

                            mNotification.notificar_radio();
                        } else if(distance <= 0.5 && distance >= 0.1){ // DANGER
                            rssi_msg.setText(mContext.getString(R.string.peligro) + "\n" + mContext.getString(R.string.mensaje_peligro));
                            res_dist.setText((int) (distance*1000) + "m");

                            // Notificamos a la víctima
                            mNotification.notificar_limite();

                            // Notificamos a los contactos
//                            mNotification.enviar_sms();
//                            mNotification.setSms_enviado(1);
                        } else if(distance < 0.1 && distance >= 0) { // URGENT
                            rssi_msg.setText("URGENTE" + "\n" + "El agresor está muy próximo a usted.");
                            res_dist.setText((int) (distance*1000) + "m");

                            // Notificamos a la víctima
                            mNotification.notificar_limite();

                            // Notificamos a los contactos
//                            mNotification.enviar_sms();
//                            mNotification.setSms_enviado(1);
                        } else if(distance == -1){ // ERROR
                            rssi_msg.setText(mContext.getString(R.string.error_distancia));
                            rssi_dist.setText("");
                        } else { // NO DANGER
                            rssi_msg.setText(mContext.getString(R.string.sin_peligro));
                            rssi_dist.setText("");
                        }

                        // Configuramos siguiente ping
                        inicio.sendPingToServer(nextPing);
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("Volley Ping Error ", error.toString());

                        TextView rssi_msg = (TextView) mActivity.findViewById(R.id.res_busqueda);
                        TextView rssi_dist = (TextView) mActivity.findViewById(R.id.res_distancia);

                        rssi_msg.setText(mContext.getString(R.string.error_distancia));
                        rssi_dist.setText("");

                        // Configuramos siguiente ping
                        inicio.sendPingToServer(10);
                    }
                });

        Config.requestQueue.add(jsonObjectRequest);
    }

    //Función que registra a un usuario en el servicio web la primera vez que usa la app
    public static void newUser(String MAC, String short_url, String server_url) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {

        // Generamos la clave secreta con la que cifrará posteriormente el AES
        String key = KeysReader.generateSharedKey(KeysReader.getPrivKeyClient(), KeysReader.getPubKeyServer());

        // Ciframos los parametros que le enviamos al servidor web
        String CMac = AESUtil.encrypt(MAC,key);
        String CServerLink = AESUtil.encrypt(short_url,key);

        // Creamos un hash con todas las variables que vamos a enviar
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("name", CMac);
        params.put("server", CServerLink);

        // Creamos el JSON y lo añadimos a la cola
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST, server_url + "/camara", new JSONObject(params),
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
    public static void streamOnline(String MAC, String name, String tlf, String lat, String lon, String short_url, String server_url) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {

        // Cogemos la fecha y hora actuales
        Time now = new Time(Time.getCurrentTimezone());
        now.setToNow();

        String minute;
        String hour;

        int min = now.minute; if(min < 10){minute = "0" + String.valueOf(min);} else { minute = String.valueOf(min); }
        int hou = now.hour; if(hou < 10){hour = "0" + String.valueOf(hou);} else { hour = String.valueOf(hou); }

        String fecha = now.monthDay + "/" + (now.month+1) + "/" + now.year + " - " + hour + ":" + minute;

        // Generamos la clave secreta con la que cifrará posteriormente el AES
        String key = KeysReader.generateSharedKey(KeysReader.getPrivKeyClient(), KeysReader.getPubKeyServer());

        // Ciframos los parametros que le enviamos al servidor web
        String CServer = AESUtil.encrypt(short_url,key);
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
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.PUT, server_url + "/online/" + MAC, new JSONObject(params),
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
    public static void streamOffline(String MAC, String server_url) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {

        // Cogemos la fecha y hora actuales
        Time now = new Time(Time.getCurrentTimezone());
        now.setToNow();

        String minute;
        String hour;

        int min = now.minute; if(min < 10){minute = "0" + String.valueOf(min);} else { minute = String.valueOf(min); }
        int hou = now.hour; if(hou < 10){hour = "0" + String.valueOf(hou);} else { hour = String.valueOf(hou); }

        String fecha = now.monthDay + "/" + (now.month+1) + "/" + now.year + " - " + hour + ":" + minute;

        // Generamos la clave secreta con la que cifrará posteriormente el AES
        String key = KeysReader.generateSharedKey(KeysReader.getPrivKeyClient(), KeysReader.getPubKeyServer());

        // Ciframos los parametros que le enviamos al servidor web
        String CFecha = AESUtil.encrypt(fecha,key);

        // Creamos un hash con todas las variables que vamos a enviar
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("date_last_online", CFecha);

        // Creamos el JSON y lo añadimos a la cola
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.PUT, server_url + "/offline/" + MAC, new JSONObject(params),
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

    /**
     * Envía las incidencias que se puedan producir al servidor
     *
     * @param info
     * @param server
     *
     * Tipo de incidencia:
     *      0 -> El móvil se va a apagar
     *      1 -> El usuario cierra la aplicación
     *      2 -> Hace tiempo que no envía ping al servidor
     *      3 -> El agresor está por debajo de 1 km de distania de la víctima
     *      4 -> EL GPS de la víctima está desactivado.
     *      5 -> No se pudo calcular la distancia entre víctima y agresor.
     *      6 -> Se pulsó el botón de pánico.
     */
    public static void sendIncidence(Map<String, String> info, String server) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        // Generamos la clave secreta con la que cifrará posteriormente el AES
        String key = KeysReader.generateSharedKey(KeysReader.getPrivKeyClient(), KeysReader.getPubKeyServer());

        // Ciframos los parametros que le enviamos al servidor web
        String CName = AESUtil.encrypt(info.get("name"),key);
        String CNumber = AESUtil.encrypt(info.get("number"),key);
        String CTypeIncidence = AESUtil.encrypt(info.get("type_incidence"),key);
        String CTextIncidence = AESUtil.encrypt(info.get("text_incidence"),key);

        // Creamos un hash con todas las variables que vamos a enviar
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("name", CName);
        params.put("number", CNumber);
        params.put("type_incidence", CTypeIncidence);
        params.put("text_incidence", CTextIncidence);

        // Creamos el JSON y lo añadimos a la cola
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.PUT, server + "/newincidence/" + info.get("mac"), new JSONObject(params),
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("Volley Ping ", response.toString());
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("Volley Ping Error ", error.toString());
                    }
                });

        Config.requestQueue.add(jsonObjectRequest);
    }

    // Avisa al servidor de que ha sido pulsado el botón de pánico
    public static void panicButtonPushed(Map<String, String> info, String server) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.PUT, server + "/panicbutton/" + info.get("mac"),
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("Volley Ping ", response.toString());
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("Volley Ping Error ", error.toString());
                    }
                });

        Config.requestQueue.add(jsonObjectRequest);
    }
}