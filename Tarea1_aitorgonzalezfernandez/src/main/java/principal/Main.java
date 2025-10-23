package principal;

import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

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
	// metodo para saber si el archivo esta vacio... lo cree ahora al final de todo
	private static boolean estaVacio(Path path) {
		try {
			return Files.size(path) == 0;
		} catch (IOException e) {
			System.out.println("No se pudo verificar el tamaño del archivo: " + e.getMessage());
			return true;
		}
	}

	public static void mostrarEspectaculos() {
		Path rutaEspectaculos = Paths.get("src/main/java/archivos/espectaculos.dat");

		// para saber si el archivo esta o no vacio o si existe o no
		if (!Files.exists(rutaEspectaculos) || estaVacio(rutaEspectaculos)) {
			System.out.println("El archivo de espectáculos está vacío o no existe.");
			return;
		}

		try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(rutaEspectaculos))) {
			System.out.println("\n--- LISTADO DE ESPECTÁCULOS ---");

			List<Espectaculo> lista = (List<Espectaculo>) ois.readObject();

			for (Espectaculo espectaculo : lista) {
				System.out.printf("ID: %d | Nombre: %s | Fecha Inicio: %s | Fecha Fin: %s%n", espectaculo.getId(),
						espectaculo.getNombre(), espectaculo.getFechaini(), espectaculo.getFechafin());
			}

		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Error al leer espectáculos.dat: " + e.getMessage());
		}
	}

	public static Optional<Credenciales> login(String nombre, String contrasenia, Path credencialesTxt) {
		// para mandar mensaje si es nulo alguno de los dos, por eso empleo el Optional
		Objects.requireNonNull(nombre, "El nombre de usuario no puede ser null");
		Objects.requireNonNull(contrasenia, "La contraseña no puede ser null");

		// para saber si es admin o no
		if (nombre.equals(ADMIN_NOMBRE) && contrasenia.equals(ADMIN_PASSWORD)) {
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
				if (nombreUsuario.equals(nombre.trim()) && contraseniaTxt.equals(contrasenia)) {
					Perfil perfil = Perfil.valueOf(perfilStr); // asume que el perfil siempre es válido
					return Optional.of(new Credenciales(id, nombreUsuario, contraseniaTxt, perfil));
				}

			}
		} catch (IOException e) {
			System.out.println("Error al leer el archivo");

		}
		return Optional.empty();
	}

	// para iniciar sesion desde el menu de invitado
	private static boolean iniciarLogin() {
		Path credencialesTxt = Paths.get("src/main/java/archivos/credenciales.txt");

		System.out.print("Usuario: "); // el .trim para eliminar los espacios en blanco al principio o al final del
		// nombre
		String nombre = leer.nextLine().trim();

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

	public static void menuInvitado() {

		System.out.println("\nBienvenido, estas accediendo como INVITADO.");
		int opcion = -1;
		do {
			System.out.println("\n--- MENU INVITADO ---");
			System.out.println("1. Ver espectaculos");
			System.out.println("2. Iniciar sesion");
			System.out.println("3. Salir");
			System.out.print("Selecciona una opcion: ");
			try {
				// probé a hacerlo asi para que el usuario pueda introducir primero un espacio
				// en blanco (por ejemplo), despues el numero y aun asi se lo pille el switch
				opcion = Integer.parseInt(leer.nextLine().trim());
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
					Sesion.cerrarSesion();
					break;
				default:
					System.out.println("La opcion no es valida. Intenta nuevamente.");
				}

			} catch (NumberFormatException e) {
				System.out.println("Debes introducir un numero valido");
			}
		} while (opcion != 3);
	}

	// -------------ADMIN-------------
	// para comprobar si existe usuario
	public static boolean repiteUsuario(String usuarioNuevo, Path rutaCredenciales) {
		try (BufferedReader reader = Files.newBufferedReader(rutaCredenciales, StandardCharsets.UTF_8)) {
			String linea;
			while ((linea = reader.readLine()) != null) {
				if (linea.trim().isEmpty())
					continue;

				String[] partes = linea.split("\\|");
				if (partes.length < 2)
					continue;

				String usuarioExistente = partes[1].trim();

				if (usuarioExistente.equalsIgnoreCase(usuarioNuevo)) {
					return true;
				}
			}
		} catch (IOException e) {
			System.out.println("Error leyendo credenciales: " + e.getMessage());
		}
		return false;
	}

	// para verificar si el email se repite
	private static boolean repiteEmail(String email, Path rutaCredenciales) {
		try (BufferedReader reader = Files.newBufferedReader(rutaCredenciales, StandardCharsets.UTF_8)) {
			String linea;
			while ((linea = reader.readLine()) != null) {
				String[] partes = linea.split("\\|");
				if (partes.length > 3 && partes[3].trim().equalsIgnoreCase(email.trim())) {
					return true;
				}
			}
		} catch (IOException e) {
			System.out.println("Error al verificar email: " + e.getMessage());
		}
		return false;
	}

	// archivo xml
	// cargar los paises
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

		System.out.println("Lista de paises disponibles:");
		for (Map.Entry<String, String> entry : paises.entrySet()) {
			System.out.printf("%s - %s%n", entry.getKey(), entry.getValue());
		}

		System.out.print("Introduce el codigo del país (ID): ");
		String idSeleccionado = leer.nextLine().trim().toUpperCase();

		if (!paises.containsKey(idSeleccionado)) {
			System.out.println("Codigo de pais no valido.");
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
		if (!nombreReal.matches("^[a-zA-ZÁÉÍÓÚáéíóúÑñüÜ\\s]+$")) {
			System.out.println("El nombre no es valido, registro cancelado.");
			return;
		}

		String email;
		while (true) {
			System.out.print("Introduce el email: ");
			email = leer.nextLine().trim();
			if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
				System.out.println("El email no es valido.");
				continue;
			}
			if (email.startsWith("|") || email.endsWith("|")) {
				System.out.println("El email no es valido");
				continue;
			}
			if (repiteEmail(email, rutaCredenciales)) {
				System.out.println("El email '" + email + "' ya está registrado. Introduce uno diferente.");
			} else {
				break;
			}
		}

		String nacionalidad = seleccionarNacionalidad();
		if (nacionalidad == null) {
			System.out.println("La nacionalidad no es valida, registro cancelado.");
			return;
		}

		String nombreUsuario;
		while (true) {
			System.out.print("Introduce el nombre de usuario (la aplicacion lo tomara como todo en minusculas): ");
			nombreUsuario = leer.nextLine().trim().toLowerCase();
			if (nombreUsuario.contains(" ") || !nombreUsuario.matches("[a-z]+") || nombreUsuario.length() < 3) {
				System.out.println(
						"El nombre de usuario no es valido. Solo letras minúsculas y sin espacios y recuerda, debe tener mas de 3 caracteres.");
				continue;
			}
			if (repiteUsuario(nombreUsuario, rutaCredenciales) || nombreUsuario.equals(ADMIN_NOMBRE)) {
				System.out.println(
						"El nombre de usuario '" + nombreUsuario + "' ya está en uso. Introduce otro diferente.");
			} else {
				break;
			}
		}

		String contrasenia;
		while (true) {
			System.out.print("Introduce la contraseña: ");
			contrasenia = leer.nextLine();
			if (contrasenia.matches(".*\\s+.*") || contrasenia.length() < 3 || contrasenia.startsWith("|")
					|| contrasenia.endsWith("|")) {
				System.out.println("La contraseña no es valida.");

			} else {
				break;
			}
		}

		System.out.print("Introduce el tipo de perfil (artista/coordinador): ");
		String perfilString = leer.nextLine().trim().toUpperCase();
		if (!perfilString.equals("ARTISTA") && !perfilString.equals("COORDINADOR")) {
			System.out.println("El perfil no es valido, registro cancelado.");
			return;
		}

		Perfil perfil = Perfil.valueOf(perfilString);
		long nuevoId = obtenerNuevoId(rutaCredenciales);

		// datos exclusivos de coordinador o artista (para mostrarlos posteriormente
		// para la confirmacion)
		String datosExtra = "";

		if (perfil == Perfil.COORDINADOR) {
			String respuesta;
			do {
				System.out.print("¿Es senior? (s/n): ");
				respuesta = leer.nextLine().trim().toLowerCase();
				if (!respuesta.equals("s") && !respuesta.equals("n")) {
					System.out.println("Opción no válida. Introduce 's' o 'n'.");
				}
			} while (!respuesta.equals("s") && !respuesta.equals("n"));

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

			datosExtra = "\nEs senior: " + (esSenior ? "Sí" : "No")
					+ (esSenior ? "\nFecha antigüedad: " + fechaSenior.toString() : "");

		} else if (perfil == Perfil.ARTISTA) {
			String tieneApodo;
			do {
				System.out.print("¿Tiene apodo? (s/n): ");
				tieneApodo = leer.nextLine().trim().toLowerCase();
				if (!tieneApodo.equals("s") && !tieneApodo.equals("n")) {
					System.out.println("Opción no válida. Introduce 's' o 'n'.");
				}
			} while (!tieneApodo.equals("s") && !tieneApodo.equals("n"));

			String apodo = null;
			if (tieneApodo.equals("s")) {
				System.out.print("Introduce el apodo: ");
				apodo = leer.nextLine().trim();
				if (!apodo.matches("^[a-zA-Z]+( [a-zA-Z]+)*$")) {
					System.out.println("El apodo no es valido.");
					return;
				}
			}

			System.out.println(
					"Selecciona las especialidades (si son varias, separalas por comas): ACROBACIA, HUMOR, MAGIA, EQUILIBRISMO, MALABARISMO");
			String especialidadesStr = leer.nextLine().trim().toUpperCase();
			String[] especialidadesArray = especialidadesStr.split(",");

			Set<Especialidad> especialidades = new HashSet<>();
			for (String esp : especialidadesArray) {
				try {
					if (!especialidades.add(Especialidad.valueOf(esp.trim()))) {
						System.out.println("Especialidad repetida: " + esp.trim());
						return;
					}
				} catch (IllegalArgumentException ex) {
					System.out.println("La especialidad no es valida: " + esp.trim() + ". Registro cancelado.");
					return;
				}
			}

			datosExtra = (apodo != null ? "\nApodo: " + apodo : "") + "\nEspecialidades: "
					+ especialidades.stream().map(Enum::name).collect(Collectors.joining(", "));
		}

		// para mostrar todos los datos
		System.out.println("\nResumen de los datos introducidos:");
		System.out.println("Nombre: " + nombreReal);
		System.out.println("Email: " + email);
		System.out.println("Usuario: " + nombreUsuario);
		System.out.println("Nacionalidad: " + nacionalidad);
		System.out.println("Perfil: " + perfilString);
		System.out.println(datosExtra);

		String confirmacion;
		do {
			System.out.print("¿Deseas guardar esta persona? (s/n): ");
			confirmacion = leer.nextLine().trim().toLowerCase();
			if (!confirmacion.equals("s") && !confirmacion.equals("n")) {
				System.out.println("Opcion no válida. Introduce 's' o 'n'.");
			}
		} while (!confirmacion.equals("s") && !confirmacion.equals("n"));

		if (confirmacion.equals("n")) {
			System.out.println("Registro cancelado por el usuario.");
			return;
		}

		// guardar los datos necesarios de credenciales.txt
		String lineaNueva = String.format("%d|%s|%s|%s|%s|%s|%s", nuevoId, nombreUsuario, contrasenia, email,
				nombreReal, nacionalidad, perfilString);

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

		// leer los espectaculos que existen, si el archivo no esta vacio
		if (Files.exists(rutaEspectaculos)) {
			try {
				if (Files.size(rutaEspectaculos) > 0) {
					try (ObjectInputStream ois = new ObjectInputStream(
							new FileInputStream(rutaEspectaculos.toFile()))) {
						listaEspectaculos.clear();
						listaEspectaculos.addAll((List<Espectaculo>) ois.readObject());
					}
				}
			} catch (IOException | ClassNotFoundException e) {
				System.out.println("No se pudo leer el archivo de espectáculos: " + e.getMessage());
				return;
			}
		}

		long nuevoId = listaEspectaculos.stream().mapToLong(Espectaculo::getId).max().orElse(0L) + 1;

		System.out.print("Introduce el nombre del espectáculo: ");
		String nombre = leer.nextLine().trim();

		if (!nombre.matches("^[a-zA-ZÁÉÍÓÚáéíóúÑñüÜ\\s]+$")) {
			System.out.println("El nombre no es valido.");
			return;
		}

		if (nombre.length() > 25) {
			System.out.println("El nombre no puede tener mas de 25 caracteres.");
			return;
		}

		boolean nombreRepetido = listaEspectaculos.stream().anyMatch(e -> e.getNombre().equalsIgnoreCase(nombre));
		if (nombreRepetido) {
			System.out.println("Ya existe un espectaculo con ese nombre.");
			return;
		}

		LocalDate fechaIni, fechaFin;
		try {
			System.out.print("Introduce la fecha de inicio (dia-mes-año): ");
			fechaIni = LocalDate.parse(leer.nextLine(), DateTimeFormatter.ofPattern("dd-MM-yyyy"));

			System.out.print("Introduce la fecha de fin (dia-mes-año): ");
			fechaFin = LocalDate.parse(leer.nextLine(), DateTimeFormatter.ofPattern("dd-MM-yyyy"));

			if (fechaFin.isBefore(fechaIni)) {
				System.out.println("La fecha de fin no puede ser anterior a la de inicio.");
				return;
			}

			if (ChronoUnit.DAYS.between(fechaIni, fechaFin) > 365) {
				System.out.println("La duración no puede superar un año.");
				return;
			}
		} catch (Exception e) {
			System.out.println("Formato de fecha incorrecto.");
			return;
		}

		Long idCoord = null;
		Sesion sesion = Sesion.getSesionActual();

		if (sesion == null) {
			// este caso de momento no deberia darse ya que al inicio de programa te inicia
			// una sesion si o si
			// y no puedes crear espectaculo si no haces login como admin o coordinador
			System.out.println("No hay sesion activa. Operación cancelada.");
			return;
		}

		Perfil perfil = sesion.getPerfil();

		if (perfil == Perfil.ADMIN) {
			// para que el admin escoja al coordinador por id
			System.out.println("Coordinadores disponibles:");
			List<Long> coordinadoresDisponibles = new ArrayList<>();

			try (BufferedReader reader = Files.newBufferedReader(rutaCredenciales, StandardCharsets.UTF_8)) {
				String linea;
				while ((linea = reader.readLine()) != null) {
					if (linea.trim().isEmpty())
						continue;

					String[] partes = linea.split("\\|");
					if (partes.length < 7)
						continue;

					if (partes[6].trim().equalsIgnoreCase("COORDINADOR")) {
						long id = Long.parseLong(partes[0].trim());
						String nombreCoord = partes[4].trim();
						System.out.printf("ID: %d - Nombre: %s%n", id, nombreCoord);
						coordinadoresDisponibles.add(id);
					}
				}
			} catch (IOException e) {
				System.out.println("Error leyendo coordinadores: " + e.getMessage());
				return;
			}
			// por si no hay coordinadores
			if (coordinadoresDisponibles.isEmpty()) {
				System.out.println("No hay coordinadores disponibles.");
				return;
			}

			System.out.print("Introduce el ID del coordinador asignado: ");
			try {
				idCoord = Long.parseLong(leer.nextLine().trim());
				if (!coordinadoresDisponibles.contains(idCoord)) {
					System.out.println("ID de coordinador no válido.");
					return;
				}
			} catch (NumberFormatException e) {
				System.out.println("ID no válido.");
				return;
			}

		} else if (perfil == Perfil.COORDINADOR) {
			// asignar id coordinador
			String nombreUsuario = sesion.getNombre();

			try (BufferedReader reader = Files.newBufferedReader(rutaCredenciales, StandardCharsets.UTF_8)) {
				String linea;
				while ((linea = reader.readLine()) != null) {
					if (linea.trim().isEmpty())
						continue;

					String[] partes = linea.split("\\|");
					if (partes.length < 7)
						continue;

					String usuario = partes[1].trim();
					String perfilStr = partes[6].trim();

					if (usuario.equalsIgnoreCase(nombreUsuario) && perfilStr.equalsIgnoreCase("COORDINADOR")) {
						idCoord = Long.parseLong(partes[0].trim());
						break;
					}
				}
			} catch (IOException e) {
				System.out.println("Error obteniendo ID del coordinador logueado: " + e.getMessage());
				return;
			}

			if (idCoord == null) {
				System.out.println("No se pudo encontrar el ID del coordinador logueado.");
				return;
			}

			System.out.println(
					"Se asigno automáticamente el espectaculo al coordinador : " + idCoord + ": " + nombreUsuario);
		} else {
			// este caso en un principio no se podria dar ya que el metodo esta implementado
			// en menuAdmin o menuCoord
			System.out.println("Tu perfil no permite crear espectáculos.");
			return;
		}

		Set<Numero> numeros = new HashSet<>();
		Espectaculo nuevo = new Espectaculo(nuevoId, nombre, fechaIni, fechaFin, idCoord, numeros);

		// para mostrar los datos
		System.out.println("\n--- Datos del espectaculo ---");
		System.out.println("ID: " + nuevoId);
		System.out.println("Nombre: " + nombre);
		System.out.println("Fecha de inicio: " + fechaIni);
		System.out.println("Fecha de fin: " + fechaFin);
		System.out.println("ID Coordinador asignado: " + idCoord);

		System.out.print("¿Deseas guardar este espectáculo? (s/n): ");
		String confirmacion = leer.nextLine().trim().toLowerCase();

		while (!confirmacion.equals("s") && !confirmacion.equals("n")) {
			System.out.print("Opción no valida. Introduce 's' o 'n': ");
			confirmacion = leer.nextLine().trim().toLowerCase();
		}

		if (confirmacion.equals("n")) {
			System.out.println("Creación del espectaculo cancelada.");
			return;
		}

		// guardar los espectaculos
		listaEspectaculos.add(nuevo);

		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(rutaEspectaculos.toFile()))) {
			oos.writeObject(listaEspectaculos);
			System.out.println("El espectaculo fue creado y guardado correctamente.");
		} catch (IOException e) {
			System.out.println("Error al guardar el espectaculo: " + e.getMessage());
		}
	}

	// este no lo pide la practica (en la primera entrega. CU4) (esta mal hecho de
	// momento)
	public static void visualizarEspectaculosCompletos() {
		Path rutaEspectaculos = Paths.get("src/main/java/archivos/espectaculos.dat");

		if (!Files.exists(rutaEspectaculos)) {
			System.out.println("No hay espectaculos registrados.");
			return;
		}

		try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(rutaEspectaculos))) {
			List<Espectaculo> lista = (List<Espectaculo>) ois.readObject();

			if (lista.isEmpty()) {
				System.out.println("No hay espectáculos en la lista.");
				return;
			}

			System.out.println("\n--- LISTADO COMPLETO DE ESPECTÁCULOS ---");

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

			for (Espectaculo e : lista) {
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
			System.out.print("Selecciona una opcion: ");

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
				System.out.println("Debes introducir un numero valido.");
			}

		} while (opcion != 4);
	}

	// -------ARTISTA-------------
	// menu artista, por el momento incompleto
	public static void menuArtista(Sesion sesion) {
		int opcion = -1;

		do {
			System.out.println("\n===== MENU ARTISTA =====");
			System.out.println("1. Ver espectaculo completo");
			System.out.println("2. Salir");
			System.out.print("Selecciona una opción: ");
			try {
				// probé a hacerlo asi para que el usuario pueda introducir primero un espacio
				// en blanco (por ejemplo), despues el numero y aun asi se lo pille el switch
				opcion = Integer.parseInt(leer.nextLine().trim());
				switch (opcion) {
				case 1:
					visualizarEspectaculosCompletos();
					break;
				case 2:
					System.out.println("Saliendo del menu de artista...");
					sesion.setPerfil(Perfil.INVITADO);
					System.out.println("¡Hasta pronto!");
					break;
				default:
					System.out.println("La opcion no es valida. Intenta nuevamente.");
				}
			} catch (NumberFormatException e) {
				System.out.println("Debes introducir un numero válido.");
			}
		} while (opcion != 2);

	}

	// ------COORDINADOR------
	// menu (incompleto por ahora)
	public static void menuCoordinador(Sesion sesion) {
		int opcion = -1;

		do {
			System.out.println("\n===== MENU COORDINADOR=====");
			System.out.println("1. Ver espectaculo completo");
			System.out.println("2. Crear espectaculo"); // cuando se avance en el codigo, pasará a ser "Gestionar
			// espectaculo" (con mas funcionalidades)
			System.out.println("3. Salir");
			System.out.print("Selecciona una opción: ");
			try {
				// probé a hacerlo asi para que el usuario pueda introducir primero un espacio
				// en blanco (por ejemplo), despues el numero y aun asi se lo pille el switch
				opcion = Integer.parseInt(leer.nextLine().trim());
				switch (opcion) {
				case 1:
					visualizarEspectaculosCompletos();
					break;
				case 2:
					crearEspectaculos();
					break;
				case 3:
					System.out.println("Saliendo del menu de coordinador...");
					sesion.setPerfil(Perfil.INVITADO);
					System.out.println("¡Hasta pronto!");
					break;
				default:
					System.out.println("La opcion no es valida. Intenta nuevamente.");
				}
			} catch (NumberFormatException e) {
				System.out.println("Debes introducir un numero válido.");
			}
		} while (opcion != 3);

	}

	public static void main(String[] args) {

		boolean confirmarSalir = false;

		Sesion.iniciarSesion(new Sesion("Invitado", Perfil.INVITADO));

		try {
			do {

				Sesion actual = Sesion.getSesionActual();

				switch (actual.getPerfil()) {
				case INVITADO:
					menuInvitado();
					break;
				case ADMIN:
					menuAdmin(actual);
					break;
				case ARTISTA:
					menuArtista(actual);
					break;
				case COORDINADOR:
					menuCoordinador(actual);
					break;

				default:
					System.out.println("Perfil no reconocido. Cerrando aplicación.");
					return;
				}
			} while (!confirmarSalir);
		} catch (NullPointerException e) {
			System.out.println("Cerrando la aplicacion del circo");
		}

	}

}
