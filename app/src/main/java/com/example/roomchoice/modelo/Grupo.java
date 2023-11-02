package com.example.roomchoice.modelo;

import java.util.ArrayList;

public class Grupo {

    private ArrayList<String> palabras;

    public Grupo(){

    }

    public Grupo(ArrayList<String> palabras){
        this.palabras = palabras;
    }

    public ArrayList<String> getPalabras() {
        return palabras;
    }

    public void setPalabras(ArrayList<String> palabras) {
        this.palabras = palabras;
    }
}
