package com.tfg.jose.proteccionpersonas;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class ContactList extends AppCompatActivity {

    private ContactArrayAdapter adaptador;
    private int PICK_CONTACT_REQUEST = 1;
    private ArrayList<Contact> contactos;
    private ListView list;

    public ContactList(){
        this.PICK_CONTACT_REQUEST = 1;
        this.contactos = new ArrayList<Contact>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.adaptador = new ContactArrayAdapter(this, android.R.layout.simple_list_item_1, contactos);

        this.list = (ListView) findViewById(R.id.lista_contactos);
        list.setAdapter(adaptador);

        // Botón flotante
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(getResources().getColorStateList(R.color.azulito));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(ContactList.this)
                        .setTitle("Añadir contacto")
                        .setMessage("Añada un contacto a su lista de avisos.")
                        // Creamos nuevo dialogo para crear un nuevo contacto
                        .setNegativeButton(R.string.nuevo, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface arg0, int arg1) {
                                LayoutInflater factory = LayoutInflater.from(ContactList.this);
                                
                                final View textEntryView = factory.inflate(R.layout.add_contact_dialog, null);

                                final EditText input1 = (EditText) textEntryView.findViewById(R.id.contact_name);
                                final EditText input2 = (EditText) textEntryView.findViewById(R.id.contact_phone);

                                final AlertDialog.Builder alert = new AlertDialog.Builder(ContactList.this);
                                alert.setTitle("Nuevo contacto").setView(textEntryView)
                                        .setPositiveButton("AÑADIR",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog,
                                                                        int whichButton) {

                                                        contactos.add(new Contact(input1.getText().toString(), input2.getText().toString()));

                                                    }
                                                }).setNegativeButton("CANCELAR",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,
                                                                int whichButton) {
                                             /*
                                             * User clicked cancel so do some stuff
                                             */
                                            }
                                        });
                                alert.show();

                            }
                        })

                        // Añadimos un contacto de la lista de contactos del móvil.
                        .setPositiveButton(R.string.existente, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface arg0, int arg1) {
                                // Abrimos los contactos para seleccionar el que deseamos añadir
                                Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
                                pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
                                startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
                            }
                        }).create().show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request it is that we're responding to
        if (requestCode == PICK_CONTACT_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

                Uri contactUri = data.getData(); // Get the URI that points to the selected contact

                String[] nombre = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
                String[] numero = {ContactsContract.CommonDataKinds.Phone.NUMBER};

                Cursor cursor_nombre = getContentResolver().query(contactUri, nombre, null, null, null);
                cursor_nombre.moveToFirst();

                Cursor cursor_numero = getContentResolver().query(contactUri, numero, null, null, null);
                cursor_numero.moveToFirst();

                // Retrieve the phone number from the NUMBER column
                int column_nombre = cursor_nombre.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                String nombre_contacto = cursor_nombre.getString(column_nombre);

                int column_numero = cursor_numero.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String numero_telefono = cursor_numero.getString(column_numero);

                Toast.makeText(ContactList.this, nombre_contacto + " - " + numero_telefono, Toast.LENGTH_SHORT).show();

                contactos.add(new Contact(nombre_contacto, numero_telefono));
                adaptador.notifyDataSetChanged();
            }
        }
    }

}
