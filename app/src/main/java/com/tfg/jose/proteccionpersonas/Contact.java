package com.tfg.jose.proteccionpersonas;

/**
 * Created by jose on 20/02/16.
 *
 * Clase Contacto.
 */

public class Contact {

    private String name;
    private String telefono;

    public Contact(String nm, String num){
        name = nm;
        telefono = num;
    }

    String getName(){
        return name;
    }

    String getNumber(){
        return telefono;
    }

    void setName(String nm){
        name = nm;
    }

    void setNumber(String num){
        telefono = num;
    }
}
