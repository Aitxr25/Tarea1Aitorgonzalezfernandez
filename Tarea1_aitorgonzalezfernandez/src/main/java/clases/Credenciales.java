package clases;

public class Credenciales {
private Long id;
private String nombre;
private String password;
private Perfil perfil;

public Credenciales() {
	
}

public Credenciales(Long id, String nombre, String password, Perfil perfil) {
	super();
	this.id = id;
	this.nombre = nombre;
	this.password = password;
	this.perfil = perfil;
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
public String getPassword() {
	return password;
}
public void setPassword(String password) {
	this.password = password;
}
public Perfil getPerfil() {
	return perfil;
}
public void setPerfil(Perfil perfil) {
	this.perfil = perfil;
}



}
