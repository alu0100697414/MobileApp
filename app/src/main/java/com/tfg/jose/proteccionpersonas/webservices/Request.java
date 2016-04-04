package com.tfg.jose.proteccionpersonas.webservices;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.tfg.jose.proteccionpersonas.StreamingConfig;
//import com.cryptull.atlas.streaming.Stream;
//import com.cryptull.atlas.streaming.StreamAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Ivan on 27/10/2015.
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
    public static void streamOnline(String MAC) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.PUT, Config.SERVER_URL + "/online/" + MAC,
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

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.PUT, Config.SERVER_URL + "/offline/" + MAC,
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