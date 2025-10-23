package clases;

public abstract class Persona {
	protected Long id;
	protected String nombre;
	protected String email;
	protected String nacionalidad;
	
	protected Credenciales credenciales;
	
	
	//PARA PERSONAS SIN CREEDENCIALES (INVITADOS)
	public Persona(Long id, String nombre, String email, String nacionalidad) {
		super();
		this.id = id;
		this.nombre = nombre;
		this.email = email;
		this.nacionalidad = nacionalidad;
	}
	
	
	
	
	//PARA PERSONAS YA LOGGEADAS
	public Persona(Long id, String nombre, String email, String nacionalidad, Credenciales credenciales) {
		super();
		this.id = id;
		this.nombre = nombre;
		this.email = email;
		this.nacionalidad = nacionalidad;
		this.credenciales = credenciales;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNacionalidad() {
		return nacionalidad;
	}

	public void setNacionalidad(String nacionalidad) {
		this.nacionalidad = nacionalidad;
	}



	public Credenciales getCredenciales() {
		return credenciales;
	}



	public void setCredenciales(Credenciales credenciales) {
		this.credenciales = credenciales;
	}
	
	
}
