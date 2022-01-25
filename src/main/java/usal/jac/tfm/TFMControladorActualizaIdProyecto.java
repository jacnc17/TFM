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
        TFMConstantes.infoSesion.forEach((key, value)->logger.info ("clave : {},} valor :  {}", key, value.getIDsesion()));

		return TFMConstantes.getInfoSesion(RequestContextHolder.currentRequestAttributes().getSessionId(), caducidad); // Recupera el id de sesión


	}
}