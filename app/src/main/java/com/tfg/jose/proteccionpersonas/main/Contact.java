package com.tfg.jose.proteccionpersonas.main;

/**
 * Created by jose on 20/02/16.
 *
 * Clase Contacto.
 */

public class Contact {

    private String name;
    private String numberphone;
    private int active;

    public Contact(String nm, String num, int act){
        name = nm;
        numberphone = num;
        active = act;
    }

    String getName(){
        return name;
    }

    String getNumber(){
        return numberphone;
    }

    int getActive(){
        return active;
    }

    void setName(String nm){
        name = nm;
    }

    void setNumber(String num){
        numberphone = num;
    }

    void setActive(int act){
        active = act;
    }
}
