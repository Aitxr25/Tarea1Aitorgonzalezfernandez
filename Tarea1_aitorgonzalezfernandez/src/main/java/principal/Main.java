package principal;

import java.util.Scanner;

import clases.Perfil;
import clases.Sesion;

public class Main {
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
