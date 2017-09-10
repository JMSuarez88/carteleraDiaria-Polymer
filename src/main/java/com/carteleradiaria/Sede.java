package com.carteleradiaria;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kaotiks on 3/10/17.
 */

public class Sede {
    private String nombre;
    private String ciudad;
    Map<String,String> cursadas = new HashMap<>();

    public Sede(String nombre) {
        this.nombre = getProperName(nombre);
        this.ciudad = getCiudadName(nombre);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Map<String, String> getCursadas() {
        return cursadas;
    }

    public void setCursadas(Map<String, String> cursadas) {
        this.cursadas = cursadas;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getCiudadName(String sedeName) {
        if (sedeName.equals("anexo") || sedeName.equals("evaperon")) {
            return "Junin";
        } else {
            return "Pergamino";
        }
    }

    public String getProperName(String sede) {
        switch (sede) {
            case "anexo":
                return "Rivadavia";
            case "evaperon":
                return "Sarmiento";
            case "monteagudo":
                return "Monteagudo";
            case "matilde":
                return "Edificio Matilde";
            case "ecana":
                return "Edificio Esc. Cs. Agrarias, Naturales Y Ambientales";
            case "inta":
                return "Pabellon Maiz (INTA)";
            default:
                return sede;
        }
    }
}
