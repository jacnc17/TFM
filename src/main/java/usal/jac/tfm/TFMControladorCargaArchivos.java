package usal.jac.tfm;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
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
			Path a;
			while (it.hasNext()) {
				a = it.next();

				
				// Añado el mime
				mimetype = URLConnection.guessContentTypeFromName(a.toString());

				// Si el mime es nulo, se intenta un método alternativo
				if (mimetype == null)
				{
					log.debug ("esVideo: mime type de {} no recuperado, probando método alternativo", a.toString());

					Path path = new File(a.toString()).toPath();
					mimetype = Files.probeContentType(path);
				}


				lista_mimes.add(mimetype	);
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
	public ArrayList<String> carga_archivos_servidor(String directorio_servidor, File directorio_temporal) {
		// Definición de variables
		ArrayList<String> lista_nombres = new ArrayList<String>();
		URL inputUrl;
		File dest; 

		// En primer lugar, los copiamos a la lista de nombres
		try {
			// Recuperamos la lista de archivos en el directorio de stickers
			ClassLoader loader = this.getClass().getClassLoader();
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(loader);
			final Resource[] resources = resolver.getResources(directorio_servidor);



			// Recorremos el listado de recursos.
			for (final Resource archivo : resources) {
				lista_nombres.add( "imagenes/stickers/" + archivo.getFilename() );
	
				// Los vamos copiando al directorio temporal
				inputUrl = getClass().getResource("/static/imagenes/stickers/" + archivo.getFilename());
				dest = new File(directorio_temporal.getAbsolutePath() + File.separator +  archivo.getFilename()); 
				FileUtils.copyURLToFile(inputUrl, dest);
				log.info ("carga_archivos_servidor: recuperado '{}', copiado a {}", archivo.getFilename(), directorio_temporal.getAbsolutePath());

			}
	}
		catch (Exception e) {
			e.printStackTrace();
		}

		return lista_nombres;
	}
}