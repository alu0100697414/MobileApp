package com.tfg.jose.proteccionpersonas;

/**
 * Created by jose on 20/02/16.
 *
 * Clase Contacto.
 */

public class Contact {

    private String name;
    private int id;

    public Contact(int num, String nm){
        name = nm;
        id = num;
    }

    String getName(){
        return name;
    }

    int getId(){
        return id;
    }

    void setName(String nm){
        name = nm;
    }

    void setId(int num){
        id = num;
    }
}
