package principal;

import java.util.Scanner;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import clases.Credenciales;
import clases.Especialidad;
import clases.Espectaculo;
import clases.Numero;
import clases.Perfil;
import clases.Sesion;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

public class Main {
	// cargar los properties
	private static final String ADMIN_NOMBRE;
	private static final String ADMIN_PASSWORD;
	static Scanner leer = new Scanner(System.in);
	private static List<Espectaculo> listaEspectaculos = new ArrayList<>();

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

	// -------------INVITADO-------------

	public static void mostrarEspectaculos() {
		Path rutaEspectaculos = Paths.get("src/main/java/archivos/espectaculos.dat");

		try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(rutaEspectaculos))) {
			System.out.println("\n--- LISTADO DE ESPECTÁCULOS ---");

			while (true) {
				try {
					Espectaculo espectaculo = (Espectaculo) ois.readObject();

					System.out.printf("ID: %d | Nombre: %s | Fecha Inicio: %s | Fecha Fin: %s%n", espectaculo.getId(),
							espectaculo.getNombre(), espectaculo.getFechaini(), espectaculo.getFechafin());

				} catch (EOFException e) {
					// para capturar la excepcion cuando se acaba el fichero .dat
					break;
				}
			}

		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Error al leer espectáculos.dat: " + e.getMessage());
		}
	}

	public static Optional<Credenciales> login(String nombre, String password, Path credencialesTxt) {
		// para mandar mensaje si es nulo alguno de los dos, por eso empleo el Optional
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
				if (line.isEmpty())
					continue;

				String[] parts = line.split("\\|");
				if (parts.length < 7)
					continue; // para comprobar si es una línea incompleta

				Long id = Long.parseLong(parts[0].trim());
				String nombreUsuario = parts[1].trim();
				String contraseniaTxt = parts[2];
				String perfilStr = parts[6].trim();

				// para comparar
				if (nombreUsuario.equals(nombre.trim()) && contraseniaTxt.equals(password)) {
					Perfil perfil = Perfil.valueOf(perfilStr); // asume que el perfil siempre es válido
					return Optional.of(new Credenciales(id, nombreUsuario, contraseniaTxt, perfil));
				}

			}
		} catch (IOException e) {
			System.out.println("Error al leer el archivo");

		}
		return Optional.empty();
	}

	public static void menuInvitado() {

		System.out.println("\nBienvenido, estas accediendo como INVITADO.");

		boolean salir = false;
		while (!salir) {
			System.out.println("\n--- MENU INVITADO ---");
			System.out.println("1. Ver espectaculos");
			System.out.println("2. Iniciar sesion");
			System.out.println("3. Salir");
			System.out.print("Selecciona una opcion: ");
			int opcion = leer.nextInt();
			// limpiar buffer
			leer.nextLine();
			switch (opcion) {
			case 1:
				mostrarEspectaculos();
				break;
			case 2:
				boolean exitoLogin = iniciarLogin(); // intenta login, redirige si tiene éxito
				if (exitoLogin)
					return; // salir del menú invitado si inicia sesión
				break;
			case 3:
				System.out.println("¡Hasta pronto!");
				salir = true;
				break;
			default:
				System.out.println("La opcion no es valida. Intentelo de nuevo.");
				break;
			}

		}
	}

	// para iniciar sesion desde el menu de invitado
	private static boolean iniciarLogin() {
		Path credencialesTxt = Paths.get("src/main/java/archivos/credenciales.txt");

		System.out.print("Usuario: ");
		String nombre = leer.nextLine();

		System.out.print("Contraseña: ");
		String password = leer.nextLine();

		Optional<Credenciales> resultado = login(nombre, password, credencialesTxt);

		if (resultado.isPresent()) {
			Credenciales cred = resultado.get();
			Sesion.iniciarSesion(new Sesion(cred.getNombre(), cred.getPerfil()));

			System.out.println("Sesión iniciada como: " + cred.getNombre());
			System.out.println("Perfil: " + cred.getPerfil());

			return true;
		} else {
			System.out.println("Credenciales incorrectas.");
			return false;
		}
	}

	// -------------ADMIN-------------
	//para comprobar si existe usuario o email
	public static boolean existeUsuarioOEmail(String usuarioNuevo, String emailNuevo, Path rutaCredenciales) {
		try (BufferedReader reader = Files.newBufferedReader(rutaCredenciales, StandardCharsets.UTF_8)) {
			String linea;
			while ((linea = reader.readLine()) != null) {
				if (linea.trim().isEmpty())
					continue;
				String[] partes = linea.split("\\|");
				if (partes.length < 7)
					continue;

				String usuarioExistente = partes[1].trim();
				String emailExistente = partes[3].trim();

				if (usuarioExistente.equalsIgnoreCase(usuarioNuevo) || emailExistente.equalsIgnoreCase(emailNuevo)) {
					return true; // ya existe
				}
			}
		} catch (IOException e) {
			System.out.println("Error leyendo credenciales: " + e.getMessage());
		}
		return false;
	}

	// archivo xml
	//cargar los paises
	public static Map<String, String> cargarPaises() {
		Map<String, String> paises = new HashMap<>();
		Path rutaPaises = Paths.get("src/main/java/archivos/paises.xml");

		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(rutaPaises.toFile());

			doc.getDocumentElement().normalize();

			NodeList listaPaises = doc.getElementsByTagName("pais");

			for (int i = 0; i < listaPaises.getLength(); i++) {
				Node nodo = listaPaises.item(i);
				if (nodo.getNodeType() == Node.ELEMENT_NODE) {
					Element elemento = (Element) nodo;

					String id = elemento.getElementsByTagName("id").item(0).getTextContent();
					String nombre = elemento.getElementsByTagName("nombre").item(0).getTextContent();

					paises.put(id, nombre);
				}
			}
		} catch (Exception e) {
			System.out.println("Error al cargar paises desde XML: " + e.getMessage());
		}

		return paises;
	}

	// metodo para elegir la nacionalidad
	public static String seleccionarNacionalidad() {
		Map<String, String> paises = cargarPaises();

		if (paises.isEmpty()) {
			System.out.println("No se pudieron cargar los países.");
			return null;
		}

		System.out.println("Lista de países disponibles:");
		for (Map.Entry<String, String> entry : paises.entrySet()) {
			System.out.printf("%s - %s%n", entry.getKey(), entry.getValue());
		}

		System.out.print("Introduce el código del país (ID): ");
		String idSeleccionado = leer.nextLine().trim().toUpperCase();

		if (!paises.containsKey(idSeleccionado)) {
			System.out.println("Código de país no válido.");
			return null;
		}

		return paises.get(idSeleccionado); // devolver el nombre completo
	}

	// para obtener un nuevo ID para la nueva persona
	public static long obtenerNuevoId(Path rutaCredenciales) {
		long maxId = 0;
		try (BufferedReader reader = Files.newBufferedReader(rutaCredenciales, StandardCharsets.UTF_8)) {
			String linea;
			while ((linea = reader.readLine()) != null) {
				if (linea.trim().isEmpty())
					continue;
				String[] partes = linea.split("\\|");
				if (partes.length < 1)
					continue;
				long id = Long.parseLong(partes[0].trim());
				if (id > maxId)
					maxId = id;
			}
		} catch (IOException e) {
			System.out.println("Error al obtener nuevo ID: " + e.getMessage());
		}
		return maxId + 1;
	}

	// metodo registrar persona (para el admin)
	public static void registrarPersona() {
		Path rutaCredenciales = Paths.get("src/main/java/archivos/credenciales.txt");

		System.out.print("Introduce nombre real: ");
		String nombreReal = leer.nextLine().trim();

		// validar nombre real (solo letras, espacios y tildes)
		if (!nombreReal.matches("^[a-zA-ZÁÉÍÓÚáéíóúÑñüÜ\\s]+$")) {
			System.out.println("El nombre no es valido, registro cancelado.");
			return;
		}

		System.out.print("Introduce email: ");
		String email = leer.nextLine();

		// validar email, tiene que contener "@" y un ".",
		// (REGEX:"^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$)"
		if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
			System.out.println("El email no es valido, registro cancelado.");
			return;
		}

		String nacionalidad = seleccionarNacionalidad();
		if (nacionalidad == null) {
			System.out.println("La nacionalidad no es valida, registro cancelado.");
			return;
		}

		System.out.print("Introduce el nombre de usuario: ");
		String nombreUsuario = leer.nextLine().trim().toLowerCase();

		// validar usuario (sin espacios, sin tildes/dieresis)
		if (nombreUsuario.contains(" ") || !nombreUsuario.matches("[a-z]+")) {
			System.out.println("El nombre de usuario no es valido.");
			return;
		}

		System.out.print("Introduce la contraseña: ");
		String contrasenia = leer.nextLine();

		// validar contraseña (para no permitir que el usuario introduzca un espacio en
		// blanco o una contraseña con algun espacio en blanco de por medio
		if (contrasenia.matches(".*\\s+.*")) {
			System.out.println("La contraseña no es valida.");
			return;
		}

		System.out.print("Introduce el tipo de perfil (artista/coordinacion): ");
		String perfilString = leer.nextLine().trim().toUpperCase();

		if (!perfilString.equals("ARTISTA") && !perfilString.equals("COORDINADOR")) {
			System.out.println("El perfil no es valido, registro cancelado.");
			return;
		}

		Perfil perfil = Perfil.valueOf(perfilString);

		if (existeUsuarioOEmail(nombreUsuario, email, rutaCredenciales)) {
			System.out.println("El nombre de usuario o email ya existen. Registro cancelado.");
			return;
		}

		long nuevoId = obtenerNuevoId(rutaCredenciales);

		if (perfil == Perfil.COORDINADOR) {
			System.out.print("¿Es senior? (s/n): ");
			String respuesta = leer.nextLine().trim().toLowerCase();
			boolean esSenior = respuesta.equals("s");
			LocalDate fechaSenior = null;

			if (esSenior) {
				System.out.print("Introduce la fecha de antigüedad (dia-mes-año): ");
				String fechaStr = leer.nextLine();
				try {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
					fechaSenior = LocalDate.parse(fechaStr, formatter);
				} catch (Exception e) {
					System.out.println("La fecha no es valida. Registro cancelado.");
					return;
				}
			}

		} else if (perfil == Perfil.ARTISTA) {
			System.out.print("¿Tiene apodo? (s/n): ");
			String tieneApodo = leer.nextLine().trim().toLowerCase();

			String apodo = null;
			if (tieneApodo.equals("s")) {
				System.out.print("Introduce el apodo: ");
				apodo = leer.nextLine().trim();

				// validar apodo (solo letras y espacios, no números ni caracteres especiales)
				if (!apodo.matches("^[a-zA-Z]+( [a-zA-Z]+)*$")) {
					System.out.println("El apodo no es valido.");
					return;
				}
			}

			System.out.println("Selecciona las especialidades: ACROBACIA, HUMOR, MAGIA, EQUILIBRISMO, MALABARISMO");
			String especialidadesStr = leer.nextLine().trim().toUpperCase();
			String[] especialidadesArray = especialidadesStr.split(",");

			Set<Especialidad> especialidades = new HashSet<>();
			for (String esp : especialidadesArray) {
				try {
					especialidades.add(Especialidad.valueOf(esp.trim()));
				} catch (IllegalArgumentException ex) {
					System.out.println("La especialidad no es valida: " + esp.trim() + ". Registro cancelado.");
					return;
				}
			}

		}

		String lineaNueva = String.format("%d|%s|%s|%s|%s|%s|%s", nuevoId, nombreUsuario, contrasenia, email,
				nombreReal, nacionalidad, perfilString);
		// el StandardOpenOption.APPEND es para que no se eliminen los datos del
		// archivo, es decir para que se sobreescriba en la siguiente linea.
		try (BufferedWriter writer = Files.newBufferedWriter(rutaCredenciales, StandardCharsets.UTF_8,
				StandardOpenOption.APPEND)) {
			writer.newLine();
			writer.write(lineaNueva);
			System.out.println("La persona fue registrada correctamente.");
		} catch (IOException e) {
			System.out.println("Error al guardar en archivo: " + e.getMessage());
		}
	}

	public static void crearEspectaculos() {
		Path rutaCredenciales = Paths.get("src/main/java/archivos/credenciales.txt");
		Path rutaEspectaculos = Paths.get("src/main/java/archivos/Espectaculos.dat");

		// leer espectáculos de Espectaculos.dat
		if (Files.exists(rutaEspectaculos)) {
			try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(rutaEspectaculos.toFile()))) {
				listaEspectaculos.clear(); // limpiar la lista estática existente para evitar duplicidad
				listaEspectaculos.addAll((List<Espectaculo>) ois.readObject()); // añadir los elementos cargados
			} catch (IOException | ClassNotFoundException e) {
				System.out.println("No se pudo leer el archivo de espectaculos: " + e.getMessage());
				return;
			}
		}

		// id
		long nuevoId = listaEspectaculos.stream().mapToLong(Espectaculo::getId).max().orElse(0L) + 1;

		System.out.print("Introduce el nombre del espectaculo: ");
		String nombre = leer.nextLine().trim();

		// validar nombre con REGEX:^[a-zA-ZÁÉÍÓÚáéíóúÑñüÜ\\s]+$
		if (!nombre.matches("^[a-zA-ZÁÉÍÓÚáéíóúÑñüÜ\\s]+$")) {
			System.out.println("El nombre no es valido.");
			return;
		}

		// validar longitud del nombre
		if (nombre.length() > 25) {
			System.out.println("El nombre del espectaculo no puede tener mas de 25 caracteres.");
			return;
		}

		// validar que no se repita el nombre
		boolean nombreRepetido = listaEspectaculos.stream().anyMatch(e -> e.getNombre().equalsIgnoreCase(nombre));
		if (nombreRepetido) {
			System.out.println("Ya existe un espectaculo con ese nombre.");
			return;
		}

		LocalDate fechaIni = null;
		LocalDate fechaFin = null;

		try {
			System.out.print("Introduce fecha de inicio (dia-mes-año): ");
			String fechaIniStr = leer.nextLine();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
			fechaIni = LocalDate.parse(fechaIniStr, formatter);

			System.out.print("Introduce fecha de fin (dia-mes-año): ");
			String fechaFinStr = leer.nextLine();
			fechaFin = LocalDate.parse(fechaFinStr, formatter);

			if (fechaFin.isBefore(fechaIni)) {
				System.out.println("La fecha de fin no puede ser anterior a la de inicio.");
				return;
			}

			// validar la duracion maxima, por eso empleo el chronounit
			if (ChronoUnit.DAYS.between(fechaIni, fechaFin) > 365) {
				System.out.println("La duracion del espectaculo no puede superar un año.");
				return;
			}
		} catch (Exception e) {
			System.out.println("El formato de fecha introducido es incorrecto.");
			return;
		}

		// mostrar los coordinadores disponibles
		List<Long> coordinadoresDisponibles = new ArrayList<>();

		try (BufferedReader reader = Files.newBufferedReader(rutaCredenciales, StandardCharsets.UTF_8)) {
			String linea;
			while ((linea = reader.readLine()) != null) {
				if (linea.trim().isEmpty())
					continue;

				String[] partes = linea.split("\\|");
				if (partes.length < 7)
					continue;

				String perfil = partes[6].trim().toUpperCase();
				if (perfil.equals("COORDINADOR")) {
					Long idCoord = Long.parseLong(partes[0].trim());
					String nombreCoord = partes[4].trim();
					System.out.printf("ID: %d - Nombre: %s%n", idCoord, nombreCoord);
					coordinadoresDisponibles.add(idCoord);
				}
			}
		} catch (IOException e) {
			System.out.println("Error al leer los coordinadores: " + e.getMessage());
			return;
		}

		if (coordinadoresDisponibles.isEmpty()) {
			System.out.println("No hay coordinadores disponibles.");
			return;
		}

		System.out.print("Introduce el ID del coordinador asignado: ");
		String idCoordStr = leer.nextLine().trim();

		Long idCoord = null;
		try {
			idCoord = Long.parseLong(idCoordStr);
			if (!coordinadoresDisponibles.contains(idCoord)) {
				System.out.println("ID de coordinador no valida.");
				return;
			}
		} catch (NumberFormatException e) {
			System.out.println("ID de coordinador no valida.");
			return;
		}

		Set<Numero> numeros = new HashSet<>();

		Espectaculo nuevo = new Espectaculo(nuevoId, nombre, fechaIni, fechaFin, idCoord, numeros);
		listaEspectaculos.add(nuevo);

		// guardar la lista actualizada en el archivo .dat
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(rutaEspectaculos.toFile()))) {
			oos.writeObject(listaEspectaculos);
			System.out.println("Espectaculo creado y guardado correctamente.");
		} catch (IOException e) {
			System.out.println("Error al guardar el espectáculo: " + e.getMessage());
		}
	}

	public static void visualizarEspectaculosCompletos() {
		Path rutaEspectaculos = Paths.get("src/main/java/archivos/espectaculos.dat");

		try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(rutaEspectaculos))) {
			System.out.println("\n--- LISTADO COMPLETO DE ESPECTÁCULOS ---");

			while (true) {
				try {
					Espectaculo e = (Espectaculo) ois.readObject();

					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

					System.out.println("ID: " + e.getId());
					System.out.println("Nombre: " + e.getNombre());
					System.out.println("Fecha Inicio: " + e.getFechaini().format(formatter));
					System.out.println("Fecha Fin: " + e.getFechafin().format(formatter));
					System.out.println("ID Coordinador: " + e.getIdCoord());

					Set<Numero> numeros = e.getNumeros();
					if (numeros == null || numeros.isEmpty()) {
						System.out.println("Numeros: No hay numeros asignados.");
					} else {
						System.out.println("Numeros asignados:");
						for (Numero n : numeros) {
							System.out.printf("  Numero ID: %d | Orden: %d | Nombre: %s | Duracion: %.2f%n", n.getId(),
									n.getOrden(), n.getNombre(), n.getDuracion());

						}
					}

					System.out.println("---------------------------------------");

				} catch (EOFException e) {
					break;
				}
			}

		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Error al leer espectaculos: " + e.getMessage());
		}
	}

	public static void menuAdmin(Sesion sesion) {
		int opcion = -1;

		do {
			System.out.println("\n===== MENU ADMINISTRADOR =====");
			System.out.println("1. Registrar nueva persona");
			System.out.println("2. Registrar nuevo espectaculo");
			System.out.println("3. Visualizar espectáculos completos");
			System.out.println("4. Salir");
			System.out.print("Selecciona una opción: ");

			try {
				// probé a hacerlo asi para que el usuario pueda introducir primero un espacio
				// en blanco (por ejemplo), despues el numero y aun asi se lo pille el switch
				opcion = Integer.parseInt(leer.nextLine().trim());

				switch (opcion) {
				case 1:
					registrarPersona();
					break;
				case 2:
					crearEspectaculos();
					break;
				case 3:
					visualizarEspectaculosCompletos();
					break;
				case 4:
					System.out.println("Saliendo del menu administrador...");
					sesion.setPerfil(Perfil.INVITADO);
					System.out.println("¡Hasta pronto!");
					break;
				default:
					System.out.println("La opcion no es valida. Intenta nuevamente.");
				}

			} catch (NumberFormatException e) {
				System.out.println("Debes introducir un numero válido.");
			}

		} while (opcion != 4);
	}

	public static void main(String[] args) {

		boolean confirmarSalir = false;
		int opcion = -1;

		// imprimirLogo(2);

		/*
		 * do { switch(actual.getPerfil()) { case INVITADO:{
		 * 
		 * } } System.out.print("Bienvenido al programa de gestion del circo");
		 * }while(confirmarsalir==false)
		 */

		Sesion.iniciarSesion(new Sesion("Invitado", Perfil.INVITADO));

		do {
			Sesion actual = Sesion.getSesionActual();

			switch (actual.getPerfil()) {
			case INVITADO:
				menuInvitado();
				break;
			case ADMIN:
				menuAdmin(actual);
				break;
				/*
				 * case ARTISTA: menuArtista(); break; case COORDINADOR: menuCoordinacion();
				 * break;
				 */
			default:
				System.out.println("Perfil no reconocido. Cerrando aplicación.");
				return;
			}
		} while (!confirmarSalir);
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
	/*	
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
		
	}while(!confirmarsalir);*/
	}
	
}
