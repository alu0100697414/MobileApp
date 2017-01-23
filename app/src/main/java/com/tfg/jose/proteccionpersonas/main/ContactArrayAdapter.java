package com.tfg.jose.proteccionpersonas.main;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tfg.jose.proteccionpersonas.R;

import java.util.ArrayList;

/**
 * Created by jose on 24/02/16.
 *
 * Clase que genera la vista para cada una de las posiciones del arrayAdapter
 */
public class ContactArrayAdapter extends ArrayAdapter<Contact> {

    private Activity mActivity;
    private ArrayList<Contact> list;


    public ContactArrayAdapter(Context context, int resource) {
        super(context, resource);
    }

    public ContactArrayAdapter(Activity context, int resource, ArrayList<Contact> items) {
        super(context, resource, items);

        this.mActivity = context;
        this.list = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        LayoutInflater inflater = mActivity.getLayoutInflater();
        View item = inflater.inflate(R.layout.contact_arrayadapter, null);

        TextView name = (TextView) item.findViewById(R.id.contacto_nombre);
        name.setText(list.get(position).getName());

        ImageView image = (ImageView) item.findViewById(R.id.estado_contacto);
        if(list.get(position).getActive() == 1){
            image.setImageResource(R.drawable.activo);
        }
        else if(list.get(position).getActive() == 0){
            image.setImageResource(R.drawable.inactivo);
        }

        return item;
    }
}
