package com.tfg.jose.proteccionpersonas;

/**
 * Created by jose on 20/02/16.
 *
 * Clase Contacto.
 */

public class Contact {

    private String name;
    private String telefono;
    private int activo;

    public Contact(String nm, String num, int act){
        name = nm;
        telefono = num;
        activo = act;
    }

    String getName(){
        return name;
    }

    String getNumber(){
        return telefono;
    }

    int getActivo(){
        return activo;
    }

    void setName(String nm){
        name = nm;
    }

    void setNumber(String num){
        telefono = num;
    }

    void setActivo(int act){
        activo = act;
    }
}
