package com.tfg.jose.proteccionpersonas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jose on 1/03/16.
 *
 * Clase para la base de datos de la aplicación.
 */

public class DBase extends SQLiteOpenHelper {

    private static final String NOMBRE_BASEDATOS = "protectULL.db";
    private static final String TABLA_CONTACTOS = "CREATE TABLE contactos (telefono TEXT PRIMARY KEY, nombre TEXT, activo INTEGER)";
    private static final int VERSION_BASEDATOS = 2;

    // Constructor de la clase
    public DBase(Context context) {
        super(context, NOMBRE_BASEDATOS, null, VERSION_BASEDATOS);

        //context.deleteDatabase(NOMBRE_BASEDATOS); // Borra la base de datos
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLA_CONTACTOS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_CONTACTOS);
        onCreate(db);
    }

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
}
