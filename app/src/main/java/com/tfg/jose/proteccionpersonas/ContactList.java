package com.tfg.jose.proteccionpersonas;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ContactList extends AppCompatActivity {

    private ContactArrayAdapter adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Creamos contactos de prueba y loss metemos en la lista para probar que funciona
        Contact a = new Contact("Jose", "689316443");
        Contact b = new Contact("Papa", "639603181");
        Contact c = new Contact("Casa", "777777777");
        Contact d = new Contact("Prueba", "999999999");

        ArrayList<Contact> contactos = new ArrayList<Contact>();

        contactos.add(a);
        contactos.add(b);
        contactos.add(c);
        contactos.add(d);

        adaptador = new ContactArrayAdapter(this, android.R.layout.simple_list_item_1, contactos);

        ListView list = (ListView) findViewById(R.id.lista_contactos);
        list.setAdapter(adaptador);

        // Botón flotante
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(getResources().getColorStateList(R.color.azulito));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

                new AlertDialog.Builder(ContactList.this)
                        .setTitle("Añadir contacto")
                        .setMessage("Añada un contacto a su lista de avisos.")
                        .setNegativeButton(R.string.nuevo, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface arg0, int arg1) {
                                Toast.makeText(ContactList.this, "Nuevo usuario", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setPositiveButton(R.string.existente, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface arg0, int arg1) {
                                // Abrimos los contactos para seleccionar el que deseamos añadir
                                Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
                                pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
                                startActivityForResult(pickContactIntent, 1);
                            }
                        }).create().show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }



}
