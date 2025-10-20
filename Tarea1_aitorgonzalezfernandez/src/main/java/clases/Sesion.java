package clases;

public class Sesion {

	private String nombre;
	private Perfil perfil;
	
	//para guardar la sesion activa
	
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
	
	// para controlar la sesion
	private static Sesion actual;

	public static void iniciarSesion(Sesion sesion) {
	    actual = sesion;
	}

	public static Sesion getSesionActual() {
	    return actual;
	}

	public static void cerrarSesion() {
	    actual = null;
	}

}
