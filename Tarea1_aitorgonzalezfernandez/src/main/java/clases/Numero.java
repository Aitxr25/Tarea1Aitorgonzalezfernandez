package clases;

import java.util.Set;

public class Numero {

	private Long id;
	private int orden;
	private String nombre;
	private double duracion;
	
	private Set<Artista> artistas;
	
	



	public Numero(Long id, int orden, String nombre, double duracion, Set<Artista> artistas) {
		super();
		this.id = id;
		this.orden = orden;
		this.nombre = nombre;
		this.duracion = duracion;
		this.artistas = artistas;
	}



	public Long getId() {
		return id;
	}



	public void setId(Long id) {
		this.id = id;
	}



	public int getOrden() {
		return orden;
	}



	public void setOrden(int orden) {
		this.orden = orden;
	}



	public String getNombre() {
		return nombre;
	}



	public void setNombre(String nombre) {
		this.nombre = nombre;
	}



	public double getDuracion() {
		return duracion;
	}



	public void setDuracion(double duracion) {
		this.duracion = duracion;
	}
	
	
	
}
