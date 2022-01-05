package usal.jac.tfm;

import java.io.File;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

@Configuration
public class TFMControladorCargaArchivos {

	private static final Logger log = LoggerFactory.getLogger(TFMControladorCargaArchivos.class);

	/**
	 * Método que recupera los ficheros del servidor para una determinada sesión y
	 * los devuelve en forma de lista. Habitualmente se llamará cuando en el navegador se haga F5.
	 */
	public ArrayList<ArrayList<String>> carga(String directorioTrabajo, String dest, String idSesion) {
		// Definición de variables
		// String idSesion;
		// Marker marcador;
		ArrayList<String> lista_nombres = new ArrayList<String>();
		ArrayList<String> lista_mimes = new ArrayList<String>();

		ArrayList<ArrayList<String>> resultado = new ArrayList<ArrayList<String>>();

		Set<Path> tmp = null;
		Stream<Path> stream = null;

		try {
			// // Recupera el id de sesión
			log.info("Recibido parámetro idSesion=" + idSesion);
			log.info("Buscando en =" + directorioTrabajo + File.separator + dest + File.separator + idSesion);

			stream = Files.list(Paths.get(directorioTrabajo + File.separator + dest + File.separator + idSesion));
			stream = stream.filter(file -> !Files.isDirectory(file));
			stream = stream.map(Path::getFileName);

			tmp = stream.map(Path::getFileName).collect(Collectors.toSet());

			Iterator<Path> it = tmp.iterator();
			String mimetype = "";
			while (it.hasNext()) {
				Path a = it.next();

				// Añado el mime
				mimetype = URLConnection.guessContentTypeFromName(a.toString());
				lista_mimes.add(URLConnection.guessContentTypeFromName(a.toString()));
				log.info("mimetype añadido = {}", mimetype);

				lista_nombres.add(dest + "/" + idSesion + "/" + a);

			}

		} catch (java.nio.file.NoSuchFileException nsfe) {
			log.error("No existe directorio para sesión " + idSesion + ". Se continúa sin cargar archivos.");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (stream != null)
				stream.close();
		}

		log.info("tmp_archivos {} ", lista_nombres);
		log.info("tmp_mimes {} ", lista_mimes);
		log.info("_-------------------------------------------------");

		resultado.add(lista_nombres);
		resultado.add(lista_mimes);

		return resultado;
	}






	/**
	 * Método que recupera los ficheros del servidor comunes a todas las sesiones  y
	 * los devuelve en forma de lista.
	 */
	public ArrayList<String> carga_archivos_servidor(String directorio_servidor) {
		ArrayList<String> lista_nombres = new ArrayList<String>();

		try {
			// Recuperamos la lista de archivos en el directorio de stickers
			ClassLoader loader = this.getClass().getClassLoader();
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(loader);
			final Resource[] resources = resolver.getResources(directorio_servidor);

			// Recorremos el listado de recursos.
			for (final Resource archivo : resources) {
				log.info ("carga_archivos_servidor: recuperado '{}'", archivo.getFilename());
				lista_nombres.add( "imagenes/stickers/" + archivo.getFilename() );
			}


	}
		catch (Exception e) {
			e.printStackTrace();
		}

		return lista_nombres;
	}
}