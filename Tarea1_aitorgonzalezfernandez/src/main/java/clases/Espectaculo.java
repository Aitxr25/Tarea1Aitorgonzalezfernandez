package clases;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class Espectaculo implements Serializable {
	private Long id;
	private String nombre;
	private LocalDate fechaini;
	private LocalDate fechafin;
	/*private Long idCoord;
	private Set<Numeros>numeros=new Set<>();*/
	
	private Long idCoord;
	private Set<Numero> numeros= new HashSet<>();
	
	
	public Espectaculo(Long id, String nombre, LocalDate fechaini, LocalDate fechafin, Long idCoord,
			Set<Numero> numeros) {
		super();
		this.id = id;
		this.nombre = nombre;
		this.fechaini = fechaini;
		this.fechafin = fechafin;
		this.idCoord = idCoord;
		this.numeros = numeros;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public LocalDate getFechaini() {
		return fechaini;
	}
	public void setFechaini(LocalDate fechaini) {
		this.fechaini = fechaini;
	}
	public LocalDate getFechafin() {
		return fechafin;
	}
	public void setFechafin(LocalDate fechafin) {
		this.fechafin = fechafin;
	}
	
	
}
