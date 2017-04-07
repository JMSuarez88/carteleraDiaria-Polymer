package com.carteleradiaria;

public class Cursada {
	private String comicion;
	private String edificio;
	private String hora;
	private String nombre;
	private String profesores;
	private String tipo;
	
	public Cursada(String comicion, String edificio, String hora, String nombre, String profesores, String tipo) {
		super();
		this.comicion = comicion;
		this.edificio = edificio;
		this.hora = hora;
		this.nombre = nombre;
		this.profesores = profesores;
		this.tipo = tipo;
	}
	public String getComicion() {
		return comicion;
	}
	public void setComicion(String comicion) {
		this.comicion = comicion;
	}
	public String getEdificio() {
		return edificio;
	}
	public void setEdificio(String edificio) {
		this.edificio = edificio;
	}
	public String getHora() {
		return hora;
	}
	public void setHora(String hora) {
		this.hora = hora;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getProfesores() {
		return profesores;
	}
	public void setProfesores(String profesores) {
		this.profesores = profesores;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	
	
}
