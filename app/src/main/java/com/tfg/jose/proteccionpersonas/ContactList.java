package com.tfg.jose.proteccionpersonas;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ContactList extends AppCompatActivity {

    private ContactArrayAdapter adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(getResources().getColorStateList(R.color.azulito));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
    }

}
