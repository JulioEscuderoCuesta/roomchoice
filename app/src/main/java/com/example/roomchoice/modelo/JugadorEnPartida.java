package com.example.roomchoice.modelo;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class JugadorEnPartida implements Serializable {

    private String email;
    private boolean haExpuesto;
    private boolean haSidoEvaluado;
    private boolean test2Terminado;
    private String historia;
    private String nombre;

    public JugadorEnPartida(){

    }

    public JugadorEnPartida(String email, boolean haExpuesto, boolean haSidoEvaluado, boolean test2Terminado, String historia, String nombre){
        this.email = email;
        this.haExpuesto = haExpuesto;
        this.haSidoEvaluado = haSidoEvaluado;
        this.test2Terminado = test2Terminado;
        this.historia = historia;
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public boolean isTest2Terminado() {
        return test2Terminado;
    }

    public void setTest2Terminado(boolean test2Terminado) {
        this.test2Terminado = test2Terminado;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isHaExpuesto() {
        return haExpuesto;
    }

    public void setHaExpuesto(boolean haExpuesto) {
        this.haExpuesto = haExpuesto;
    }

    public boolean isHaSidoEvaluado() {
        return haSidoEvaluado;
    }

    public void setHaSidoEvaluado(boolean haSidoEvaluado) {
        this.haSidoEvaluado = haSidoEvaluado;
    }

    public String getHistoria() {
        return historia;
    }

    public void setHistoria(String historia) {
        this.historia = historia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("email", email);
        result.put("haExpuesto", haExpuesto);
        result.put("haSidoEvaluado", haSidoEvaluado);
        result.put("test2Terminado", test2Terminado);
        result.put("nombre", nombre);


        return result;
    }
}
