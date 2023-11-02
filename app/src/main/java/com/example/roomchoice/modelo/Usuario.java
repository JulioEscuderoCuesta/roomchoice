package com.example.roomchoice.modelo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Usuario {

    private String nombre;
    private String email;
    private String telefono;
    private Rol rol;
    public Usuario() {

    }

    public Usuario(String nombre, String email, String telefono) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        rol = Rol.EMPLEADO;
        JSONObject json = new JSONObject();
        try {
            json.put("nombre", "nombre");
            json.put("email", email);
            json.put("telefono", telefono);
        } catch (JSONException e) {
            //Mensaje de error
        }
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }

    public Rol getRol() {
        return rol;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
