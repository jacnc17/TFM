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
import usal.jac.tfm.TFMUtils.TFMUtils;

@RestController

public class TFMControladorGetInfoOperacion {

	@Value("${file.dirTrabajo}")
	String directorioTrabajo;

	@Value("${file.uploadDir}")
	String dest;

	@Value("${server.servlet.session.timeout}")
	String caducidad;

	@Value("${file.rutaFFPROBRE}")
	String rutaFFPROBRE;

	private static final Logger logger = LoggerFactory.getLogger(TFMControladorGetInfoOperacion.class);

	/**
	 * Método GET para recuperar el estado de un render de vídeo (sólo uno por sesión)
	 */
	@GetMapping("/getInfoOperacion")
	public String recupera_info() throws IOException {
		// Definición de variables
		String idSesion = TFMConstantes.getInfoSesion(RequestContextHolder.currentRequestAttributes().getSessionId(), caducidad);
		String resultado = "";

		//logger.info ("getInfoOperacion");

        resultado = TFMUtils.getAvanceOperacion(idSesion);

		return resultado;
	}
}