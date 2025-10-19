package principal;

import java.util.Scanner;
import clases.Credenciales;
import clases.Perfil;
import clases.Sesion;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

public class Main {
	//cargar los properties
	private static final String ADMIN_NOMBRE;
	private static final String ADMIN_PASSWORD;

	static {
	    Properties props = new Properties();
	    try (InputStream input = Files.newInputStream(Paths.get("src/main/resources/application.properties"))) {
	        props.load(input);
	    } catch (IOException e) {
	        System.out.println("No se pudo cargar application.properties: " + e.getMessage());
	    }

	    ADMIN_NOMBRE = props.getProperty("admin.nombre", "").trim();
	    ADMIN_PASSWORD = props.getProperty("admin.password", "").trim();
	}

	
	public static Optional<Credenciales> login(String nombre, String password, Path credencialesTxt) {
        //para mandar mensaje si es nulo alguno de los dos, por eso empleo el optional
		Objects.requireNonNull(nombre, "El nombre de usuario no puede ser null"); 
        Objects.requireNonNull(password, "La contraseña no puede ser null");
        
        
     // para saber si es admin o no
        if (nombre.equals(ADMIN_NOMBRE) && password.equals(ADMIN_PASSWORD)) {
            return Optional.of(new Credenciales(0L, ADMIN_NOMBRE, ADMIN_PASSWORD, Perfil.ADMIN));
        }
 
        try (BufferedReader reader = Files.newBufferedReader(credencialesTxt, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|");
                if (parts.length < 7) continue; // para comprobar si es una línea incompleta

                Long id = Long.parseLong(parts[0].trim());
                String nombreUsuario = parts[1].trim();
                String contraseniaTxt = parts[2];
                String perfilStr = parts[6].trim();

                // Comparar
                if (nombreUsuario.equals(nombre.trim()) && contraseniaTxt.equals(password)) {
                    Perfil perfil = Perfil.valueOf(perfilStr); // asume que el perfil siempre es válido
                    return Optional.of(new Credenciales(id, nombreUsuario, contraseniaTxt, perfil));
                }
                
            }
        }catch(IOException e) {
    System.out.println("Error al leer el archivo");
    

       
    }
		 return Optional.empty();
    }
	
	public static void main(String[]args) {
		Sesion actual=new Sesion("invitado",Perfil.INVITADO);
		boolean confirmarsalir = false;
		int opcion=-1;
		Scanner leer=new Scanner (System.in);
		//imprimirLogo(2);
		
		/*do {
			switch(actual.getPerfil()) {
			case INVITADO:{
				
			}
			}
			System.out.print("Bienvenido al programa de gestion del circo");
		}while(confirmarsalir==false)*/
			
	do {
		switch(actual.getPerfil()) {
		case INVITADO:{
		System.out.print("Bienvenido al programa de gestion del circo");
		System.out.print("1. Ver espectaculo");
		System.out.print("2. Login");
		System.out.print("3. Salir");
		
		System.out.println("Elija la opcion deseada");
		opcion=leer.nextInt();
		
		switch(opcion) {
		case 1:
			//Una vez creada la lista de espectaculos recorrerla con el metodo mostrarEspectaculos que esta en la clase de Espectaculo
			
			
			break;
		
		case 2:
			
			String nombre=leer.next();
			String password=leer.next();
			
			//Comprobar si coincide con el documento txt los credenciales del usuario
			
			break;
			
		case 3:
			System.out.println("Adios!");
			confirmarsalir=true;
			
		}
		}
		}
		
	}while(!confirmarsalir);
	}
	
}
