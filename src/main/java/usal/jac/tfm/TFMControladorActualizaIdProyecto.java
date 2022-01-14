package usal.jac.tfm;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;

import usal.jac.tfm.TFMUtils.TFMConstantes;

@RestController

public class TFMControladorActualizaIdProyecto {
	@Value("${file.dirTrabajo}")
	String directorioTrabajo;

	@Value("${file.uploadDir}")
	String dest;

	@Value("${server.servlet.session.timeout}")
	String caducidad;

	@Value("${file.rutaFFPROBRE}")
	String rutaFFPROBRE;

	private static final Logger logger = LoggerFactory.getLogger(TFMControladorActualizaIdProyecto.class);

	/**
	 * Método GET para recuperar la duración de un vídeo cuyo nombre se pasa por parámetro en la petición GET
	 */
	@GetMapping("/actualizaIdProyecto/{id}")
	public String recupera_info(@PathVariable String id) throws IOException {
        logger.info ("ID RECUPERADO {}", id);

        // public static Hashtable<String, TFMInfoSesion> infoSesion = new Hashtable<String, TFMInfoSesion>();
        // Recorremos la hash de sesiones
/*         for (int i = 0; i<TFMConstantes.infoSesion.size(); i++)
        {
            
            logger.info ("sesion {}", id);

        } */

        TFMConstantes.infoSesion.forEach((key, value)->logger.info ("clave : {},} valor :  {}", key, value.getIDsesion()));

		return TFMConstantes.getInfoSesion(RequestContextHolder.currentRequestAttributes().getSessionId(), caducidad); // Recupera el id de sesión

        

        /// return "papaito";

/* 
		// Definición de variables
		String idSesion = TFMConstantes.getInfoSesion(RequestContextHolder.currentRequestAttributes().getSessionId(), caducidad);
		String resultado = "";
		Process p; // Variable de proceso con la que se ejecutará ffprobe

		logger.info ("Recuperando duración del archivo de vídeo '{}' mediante ffprobe.", id);

		// Nombre final del archivo cuya info queremos recuperar.
		Path destinationFile = Paths
				.get(directorioTrabajo + File.separator + dest + File.separator + idSesion + File.separator + id)
				.toAbsolutePath().normalize();

		// Se lanza la petición ffprobe
		p = new ProcessBuilder(Arrays.asList(rutaFFPROBRE, "-v", "error", "-show_entries", "format=duration",
				"-of", "default=noprint_wrappers=1:nokey=1", destinationFile.toString())).start();
		resultado = IOUtils.toString(p.getInputStream(), Charset.defaultCharset());

		// Capturamos y devolvemos la duración del archivo en segundos (con precisión de millonésima de segundo! : 12.24000)
		while (resultado.equals(""))
		{


			logger.info ("Duración de vídeo '{}' no recuperada. Reintentando. ", id	);

			try{Thread.sleep(2000); } catch (Exception e) { e.printStackTrace(); } // TODO: paralelizar! Controlar número reintentos.
	
			destinationFile = Paths
				.get(directorioTrabajo + File.separator + dest + File.separator + idSesion + File.separator + id)
				.toAbsolutePath().normalize();

			p = new ProcessBuilder(Arrays.asList(rutaFFPROBRE, "-v", "error", "-show_entries", "format=duration",
				"-of", "default=noprint_wrappers=1:nokey=1", destinationFile.toString())).start();
			resultado = IOUtils.toString(p.getInputStream(), Charset.defaultCharset());


		}
		


		logger.info ("Duración de vídeo '{}' = {}", id, resultado);

		return id+","+resultado;
         */
	}
}