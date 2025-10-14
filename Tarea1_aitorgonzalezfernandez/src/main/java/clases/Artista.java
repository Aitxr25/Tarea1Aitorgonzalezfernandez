package clases;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Artista extends Persona{
	
	private Long idArt;
	private String apodo = null;
	private Set <Especialidad> especialidades;
	
	public Artista(Long id, String nombre, String email, String nacionalidad) {
		super(id, nombre, email, nacionalidad);
		// TODO Auto-generated constructor stub
	}
	
	
	public Artista(Long id, String nombre, String email, String nacionalidad, Long idArt, String apodo,
			Set<Especialidad> especialidades, List<Numero> numeros) {
		super(id, nombre, email, nacionalidad);
		this.idArt = idArt;
		this.apodo = apodo;
		this.especialidades = especialidades;
		this.numeros = numeros;
	}


	private List<Numero> numeros = new ArrayList<>();
	public Long getIdArt() {
		return idArt;
	}
	public void setIdArt(Long idArt) {
		this.idArt = idArt;
	}
	public String getApodo() {
		return apodo;
	}
	public void setApodo(String apodo) {
		this.apodo = apodo;
	}
	public Set<Especialidad> getEspecialidades() {
		return especialidades;
	}
	public void setEspecialidades(Set<Especialidad> especialidades) {
		this.especialidades = especialidades;
	}
	
}
