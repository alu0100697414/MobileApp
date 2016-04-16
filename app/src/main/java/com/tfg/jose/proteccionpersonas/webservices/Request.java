package com.tfg.jose.proteccionpersonas.webservices;

import android.text.format.Time;
import android.util.Log;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.tfg.jose.proteccionpersonas.StreamingConfig;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Jose Angel.
 */

public class Request {

    //Función que registra a un usuario en el servicio web la primera vez que usa la app
    public static void newUser(String MAC) {

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("name", MAC);
        params.put("server", StreamingConfig.STREAM_SHORT_URL);

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
    public static void streamOnline(String MAC, String name, String tlf) {

        Time now = new Time(Time.getCurrentTimezone());
        now.setToNow();

        String minute;
        String hour;

        int min = now.minute; if(min < 10){minute = "0" + String.valueOf(min);} else { minute = String.valueOf(min); }
        int hou = now.hour; if(hou < 10){hour = "0" + String.valueOf(hou);} else { hour = String.valueOf(hou); }

        String fecha = now.monthDay + "/" + (now.month+1) + "/" + now.year + " - " + hour + ":" + minute;

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("name", MAC);
        params.put("server", StreamingConfig.STREAM_SHORT_URL);
        params.put("nombre", name);
        params.put("numero", tlf);
        params.put("time_now", fecha);

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
    public static void streamOffline(String MAC) {

        Time now = new Time(Time.getCurrentTimezone());
        now.setToNow();

        String minute;
        String hour;

        int min = now.minute; if(min < 10){minute = "0" + String.valueOf(min);} else { minute = String.valueOf(min); }
        int hou = now.hour; if(hou < 10){hour = "0" + String.valueOf(hou);} else { hour = String.valueOf(hou); }

        String fecha = now.monthDay + "/" + (now.month+1) + "/" + now.year + " - " + hour + ":" + minute;

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("date_last_online", fecha);

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