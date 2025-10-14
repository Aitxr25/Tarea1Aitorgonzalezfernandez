package clases;

public class Sesion {

	private String nombre;
	private Perfil perfil;
	public Sesion(String nombre, Perfil perfil) {
		super();
		this.nombre = nombre;
		this.perfil = perfil;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public Perfil getPerfil() {
		return perfil;
	}
	public void setPerfil(Perfil perfil) {
		this.perfil = perfil;
	}
	
	
}
