package com.example.roomchoice.modelo;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Partida implements Serializable {

    private String fechaCreacion;
    private int maxParticipantes;
    private Estado estado;
    private String codigo;
    private int jugActuales;
    private ArrayList<Usuario> jugadores;

    private Partida() {

    }

    public Partida(String codigo, String fechaCreacion, int maxParticipantes, int jugActuales, ArrayList<Usuario> jugadores) {
        this.codigo = codigo;
        this.fechaCreacion = fechaCreacion;
        this.maxParticipantes = maxParticipantes;
        this.estado = Estado.CREADA;
        this.jugadores = jugadores;
        this.jugActuales = jugActuales;
        //salas = new ArrayList<>();
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public void setJugadores(ArrayList<Usuario> jugadores) {
        this.jugadores = jugadores;
    }

    public void setMaxParticipantes(int maxParticipantes) {
        this.maxParticipantes = maxParticipantes;
    }

    public int getJugActuales() {
        return jugActuales;
    }

    public void setJugActuales(int jugActuales) {
        this.jugActuales = jugActuales;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public int getMaxParticipantes() {
        return maxParticipantes;
    }

    public Estado getEstado() {
        return estado;
    }

    public String getCodigo() {
        return codigo;
    }

    public ArrayList<Usuario> getJugadores() {
        return jugadores;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("codigo", codigo);
        result.put("fechaCreacion", fechaCreacion);
        result.put("maxParticipantes", maxParticipantes);
        result.put("estado", estado);
        result.put("jugActuales", jugActuales);


        return result;
    }

}
