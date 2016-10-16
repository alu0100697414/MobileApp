package com.tfg.jose.proteccionpersonas.main;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by jose on 1/03/16.
 *
 * Clase para la base de datos de la aplicación.
 */

public class DBase extends SQLiteOpenHelper {

    private static final String NOMBRE_BASEDATOS = "protectULL.db";
    private static final String TABLA_CONTACTOS = "CREATE TABLE contactos (telefono TEXT PRIMARY KEY, nombre TEXT, activo INTEGER)";
    private static final String TABLA_INFO_USUARIO = "CREATE TABLE tabla_info_usuario (telefono TEXT PRIMARY KEY, nombre TEXT)";
    private static final String TABLA_CONFIG_APP = "CREATE TABLE tabla_config_app (id TEXT PRIMARY KEY, password TEXT)";
    private static final String TABLA_INFO_SERVIDOR = "CREATE TABLE tabla_info_servidor (id TEXT PRIMARY KEY, server_url TEXT, url_streaming TEXT, user_server TEXT, pass_server TEXT, short_url TEXT)";
    private static final int VERSION_BASEDATOS = 7;

    // Constructor de la clase
    public DBase(Context context) {
        super(context, NOMBRE_BASEDATOS, null, VERSION_BASEDATOS);

//        context.deleteDatabase(NOMBRE_BASEDATOS); // Borra la base de datos
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLA_CONTACTOS);
        db.execSQL(TABLA_INFO_USUARIO);
        db.execSQL(TABLA_CONFIG_APP);
        db.execSQL(TABLA_INFO_SERVIDOR);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_CONTACTOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_INFO_USUARIO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_CONFIG_APP);
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_INFO_SERVIDOR);
        onCreate(db);
    }

    // METODOS DE LA TABLA INFO USUARIO
    // Inserta un contacto nuevo
    public void insertarCONTACTO(String tlf, String nom, int act) {
        tlf = tlf.replace(" ","");
        tlf = tlf.replace("+34","");

        SQLiteDatabase db = getWritableDatabase();
        if(db != null){
            ContentValues valores = new ContentValues();
            valores.put("telefono", tlf);
            valores.put("nombre", nom);
            valores.put("activo", act);
            db.insert("contactos", null, valores);
            db.close();
        }
    }

    // Edita un contacto
    public void modificarCONTACTO(String tlf, String nom, int act){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("nombre", nom);
        valores.put("telefono", tlf);
        valores.put("activo", act);
        db.update("contactos", valores, "telefono=" + tlf, null);
        db.close();
    }

    // Borra un contacto
    public void borrarCONTACTO(String tlf) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("contactos", "telefono=" + tlf, null);
        db.close();
    }

    // Devuelve un contacto
    public Contact recuperarCONTACTO(String tlf) {
        SQLiteDatabase db = getReadableDatabase();
        String[] valores_recuperar = {"telefono", "nombre"};
        Cursor c = db.query("contactos", valores_recuperar, "telefono=" + tlf, null, null, null, null, null);
        if(c != null) {
            c.moveToFirst();
        }
        Contact contactos = new Contact(c.getString(1), c.getString(0), c.getInt(2));
        db.close();
        c.close();

        return contactos;
    }

    // Devuelve todos los contactos
    public List<Contact> recuperarCONTACTOS() {

        SQLiteDatabase db = getReadableDatabase();
        List<Contact> lista_contactos = new ArrayList<Contact>();
        String[] valores_recuperar = {"telefono", "nombre", "activo"};
        Cursor c = db.query("contactos", valores_recuperar, null, null, null, null, null, null);

        if(c.getCount() != 0){
            c.moveToFirst();

            do {
                Contact contactos = new Contact(c.getString(1), c.getString(0), c.getInt(2));
                lista_contactos.add(contactos);
            } while (c.moveToNext());
        }

        db.close();
        c.close();

        return lista_contactos;
    }


    // METODOS DE LA TABLA INFO USUARIOS
    // Inserta un contacto nuevo
    public void insertarINFO_USUARIO(String tlf, String nom) {
        tlf = tlf.replace(" ","");
        tlf = tlf.replace("+34","");

        SQLiteDatabase db = getWritableDatabase();
        if(db != null){
            ContentValues valores = new ContentValues();
            valores.put("telefono", tlf);
            valores.put("nombre", nom);
            db.insert("tabla_info_usuario", null, valores);
            db.close();
        }
    }

    // Edita un contacto
    public void modificarINFO_USUARIO(String tlf, String nom){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("nombre", nom);
        valores.put("telefono", tlf);
        db.update("tabla_info_usuario", valores, tlf, null);
        db.close();
    }

    // Borra un contacto
    public void borrarINFO_USUARIO(String tlf) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("tabla_info_usuario", tlf, null);
        db.close();
    }

    // Devuelve un contacto
    public Contact recuperarINFO_USUARIO(String tlf) {
        SQLiteDatabase db = getReadableDatabase();
        String[] valores_recuperar = {"telefono", "nombre"};
        Cursor c = db.query("tabla_info_usuario", valores_recuperar, tlf, null, null, null, null, null);
        if(c != null) {
            c.moveToFirst();
        }
        Contact contacto = new Contact(c.getString(1), c.getString(0), 1);
        db.close();
        c.close();

        return contacto;
    }

    // Devuelve todos los contactos
    public List<Contact> recuperarINFO_USUARIO() {

        SQLiteDatabase db = getReadableDatabase();
        List<Contact> lista_contactos = new ArrayList<Contact>();
        String[] valores_recuperar = {"telefono", "nombre"};
        Cursor c = db.query("tabla_info_usuario", valores_recuperar, null, null, null, null, null, null);

        if(c.getCount() != 0){
            c.moveToFirst();

            do {
                Contact contactos = new Contact(c.getString(1), c.getString(0), 1);
                lista_contactos.add(contactos);
            } while (c.moveToNext());
        }

        db.close();
        c.close();

        return lista_contactos;
    }

    // METODOS DE LA TABLA CONFIG APP
    // Inserta la contraseña de la app
    public void insertarCONFIG_APP(String id, String pass) {
        SQLiteDatabase db = getWritableDatabase();
        if(db != null){
            ContentValues valores = new ContentValues();
            valores.put("id", id);
            valores.put("password", pass);
            db.insert("tabla_config_app", null, valores);
            db.close();
        }
    }

    // Edita la contraseña de la app
    public void modificarCONFIG_APP(String id, String pass){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("id", id);
        valores.put("password", pass);
        db.update("tabla_config_app", valores, id, null);
        db.close();
    }

    // Devuelve la contraseña
    public String recuperarCONFIG_APP(String id) {

        SQLiteDatabase db = getReadableDatabase();
        String[] valores_recuperar = {"password"};
        Cursor c = db.query("tabla_config_app", valores_recuperar, id, null, null, null, null, null);

        String pass = "";
        if(c != null) {
            c.moveToFirst();
            if(c.getCount() > 0){
                pass = c.getString(0);
            }
        }

        db.close();
        c.close();

        return pass;
    }


    // METODOS DE LA TABLA INFO SERVER
    // Inserta la info del server
    public void insertarINFO_SERVER(String id, String url_server, String streaming_url, String server_user, String pass_server, String shor_url) {
        SQLiteDatabase db = getWritableDatabase();
        if(db != null){
            ContentValues valores = new ContentValues();
            valores.put("id", id);
            valores.put("server_url", url_server);
            valores.put("url_streaming", streaming_url);
            valores.put("user_server", server_user);
            valores.put("pass_server", pass_server);
            valores.put("short_url", shor_url);
            db.insert("tabla_info_servidor", null, valores);
            db.close();
        }
    }

    // Edita la info del server
    public void modificarINFO_SERVER(String id, String url_server, String streaming_url, String server_user, String pass_server, String shor_url){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("id", id);
        valores.put("server_url", url_server);
        valores.put("url_streaming", streaming_url);
        valores.put("user_server", server_user);
        valores.put("pass_server", pass_server);
        valores.put("short_url", shor_url);
        db.update("tabla_info_servidor", valores, id, null);
        db.close();
    }

    // Devuelve la info del server
    public List<String> recuperarINFO_SERVER(String id) {

        SQLiteDatabase db = getReadableDatabase();
        String[] valores_recuperar = {"server_url","url_streaming","user_server","pass_server","short_url"};
        Cursor c = db.query("tabla_info_servidor", valores_recuperar, id, null, null, null, null, null);

        List<String> info = new ArrayList<String>();
        if(c != null) {
            c.moveToFirst();
            if(c.getCount() > 0){
                info.add(c.getString(0));
                info.add(c.getString(1));
                info.add(c.getString(2));
                info.add(c.getString(3));
                info.add(c.getString(4));
            }
        }

        db.close();
        c.close();

        return info;
    }
}

