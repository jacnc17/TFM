package usal.jac.tfm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import usal.jac.tfm.TFMUtils.TFMConstantes;
import usal.jac.tfm.TFMUtils.TFMUtils;
import usal.jac.tfm.excepciones.TFMExcepcionArchivo;

@RestController
@RequestMapping("/upload")
public class TFMControladorSubeArchivo {
	@Value("${file.dirTrabajo}")
	String directorioTrabajo;

	@Value("${file.uploadDir}")
	String dest;

	@Value("${file.rutaFFMPEG}")
	String rutaFFMPEG;

	private static final Logger log = LoggerFactory.getLogger(TFMControladorSubeArchivo.class);

	private String dirDestino = ""; // Ubicación de los archivos en el servidor, diferente para cada usuario.

	/**
	 * Método llamado desde la página principal para almacenar temporalmente los
	 * archivos con los que se va a hacer la edición.
	 * 
	 * @param file Archivo, en formato MultipartFile, que será almacenado en el
	 *             servidor.
	 */
	@PostMapping
	public void upload(@RequestParam("file") MultipartFile file) {
		// Definición de variables
		String idSesion;
		Marker marcador;

		try {
			// idSesion = RequestContextHolder.currentRequestAttributes().getSessionId(); // Recupera el id de sesión

			idSesion = TFMConstantes.getInfoSesion(RequestContextHolder.currentRequestAttributes().getSessionId()); // Recupera el id de sesión

			log.error("idSesion=" + idSesion);

			// TODO: realizar validaciones previar del formato y contenido del archivo, de
			// su tamaño, etc.

			// Obtiene un marcador con el id de sesión
			marcador = MarkerFactory.getMarker(idSesion);
			try {

				// Llama al método de almacenaje y crea las miniaturas asociadas
				almacena(file, idSesion, marcador);

				log.info(marcador, "Archivo subido correctamente: " + file.getOriginalFilename());
			} catch (TFMExcepcionArchivo tfmea) {
				log.error(marcador, "El proceso de subida finaliza con error.");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Método que almacena los archivos que se dejen en la dropzone de la página
	 * principal.
	 * 
	 * @param file     Contenido a almacenar
	 * @param idSesion Identificador de sesión del usuario, servirá para crear un
	 *                 directorio único para cada usuario donde depositar los
	 *                 archivos.
	 * @param marca    Marca de logs.
	 */
	private void almacena(MultipartFile file, String idSesion, Marker marca) throws TFMExcepcionArchivo {
		// Definición de variables
		String nombre_archivo =  "";
		String nombre_final = "";


		try {
			// Si el archivo está vacío.
			if (file.isEmpty()) {
				log.error(marca, "Hay un problema con el archivo - parece que es null");
				throw new TFMExcepcionArchivo("Error almacenando archivo nulo o vacío.");
			}

			// Verifica que el directorio de destino existe (y lo crea si no). También crea
			// el directorio de salida
			dirDestino = TFMUtils.creaDirectorio(directorioTrabajo, dest);

			// Si existe el directorio general de destino
			if (dirDestino != null) {
				// Se crea el directorio de destino para esta sesión
				String dirDestinoSesion = TFMUtils.creaDirectorio(dirDestino, idSesion);

				// Si existe el directorio de destino para esta sesión.
				if (dirDestinoSesion != null) {
					log.info("Directorio destino creado correctamente  " + dirDestinoSesion);

					// Se almacena con el tamaño para permitir archivos del mismo nombre
					// Atención: para evitar errores, se reemplazan los espacios en blanco por _ .
					nombre_archivo = file.getOriginalFilename().replace(' ','_');
					nombre_final = file.getSize() + "_" + nombre_archivo;
					log.info("nombreFinal  " + nombre_final);

					// Nombre final del archivo a subir.
					Path destinationFile = Paths
							.get(dirDestinoSesion + File.separator + nombre_final)
							.toAbsolutePath().normalize();

					// Se inicia el proceso de almacenamiento en disco del servidor.
					try (InputStream inputStream = file.getInputStream()) {
						// Guardando en disco.
						Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
						log.info("Archivo " + file.getOriginalFilename() + " guardado correctamente.");

						// Creación de thumbnails
						// En primer lugar se determinará si el archivo es vídeo
						String dirDestinoMiniatura = TFMUtils.creaDirectorio(dirDestinoSesion,
								TFMConstantes.dirMiniaturas);

						// Se genera el directorio destino de la miniatura
						// (true), si es imagen (false) u otro (excepción)
						if (TFMUtils.esVideo(nombre_archivo)) {

							TFMUtils.generaMiniaturaVideo(file, dirDestinoMiniatura, destinationFile.toString(), rutaFFMPEG);

						} else // Se trata de un archivo de imagen
						{
							TFMUtils.generaMiniaturaImagen(file, dirDestinoMiniatura);
						}
						inputStream.close();

					}
				} else {
					log.error("NO SE PUDO CREAR UN DIRECTORIO PARA DEPOSITAR LOS ARCHIVOS DE LA SESIÓN" + idSesion
							+ "!!");
				}
			} else {
				log.error("NO SE PUDO CREAR UN DIRECTORIO GENÉRICO PARA DEPOSITAR LOS ARCHIVOS!!");
			}
		} catch (IOException e) {
			throw new TFMExcepcionArchivo("Error almacenando archivos!.", e);
		}
	}
}