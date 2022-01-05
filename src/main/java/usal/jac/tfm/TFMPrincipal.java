package usal.jac.tfm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.RequestContextHolder;

import usal.jac.tfm.TFMUtils.TFMConstantes;
import usal.jac.tfm.TFMUtils.TFMUtils;


@Controller
@SpringBootApplication
public class TFMPrincipal {
	// @Value("${spring.web.resources.static-locations}")
	@Value("${file.dirTrabajo}")
	String directorioTrabajo;

	@Value("${file.uploadDir}")
	String dest;

	static boolean purgado = false;

	private static final Logger log = LoggerFactory.getLogger(TFMPrincipal.class);

	public static void main(String[] args) {
		SpringApplication.run(TFMPrincipal.class, args);

		log.info("ARRANCANDO APLICACIÓN!!!!!!!!!!!!!!!");

	}



	@GetMapping("/")
	public String inicio(Model model) throws IOException {

		// Se borran los directorios temporales de la anterior ejecución -> evitamos saturar el servidor.
		if (!purgado)
		{
			log.info("_____________________________________ purgando {} ", directorioTrabajo + File.separator + dest);
			purgado = true;
			FileUtils.deleteDirectory (new File(directorioTrabajo + File.separator + dest));
		}


		// OK String idSesion = RequestContextHolder.currentRequestAttributes().getSessionId(); // Recupera el id de sesión
		String idSesion = TFMConstantes.getInfoSesion(RequestContextHolder.currentRequestAttributes().getSessionId()); // Recupera el id de sesión

		// Se comprueba si esta sesión está ya registrada
		if (idSesion == null)
		{
			// Si no lo estaba, se genera una nueva sesión. Para el usuario se manejará este id interno de sesión
			idSesion = TFMConstantes.anadeSesion (RequestContextHolder.currentRequestAttributes().getSessionId());
		} 

		log.info("ARRANCANDO CON " + idSesion + ", en " + directorioTrabajo + File.separator + dest);
		log.info("Inicializando variables generales");
		TFMConstantes.inicializaConstantes();

		// Para este identificador de sesión específico, determinamos si existen
		// archivos para esta sesión. Si los hay, los almacena en el modelo
		// "misArchivos" para que puedan ser recuperados vía thymeleaf
		ArrayList<ArrayList<String>> info_archivos = new TFMControladorCargaArchivos().carga(directorioTrabajo, dest, idSesion);
		model.addAttribute("misArchivos", info_archivos.get(0) );

		log.info("misArchivos {}", info_archivos.get(0));
		log.info("misMimes {}", info_archivos.get(1));

		model.addAttribute("misMimes", info_archivos.get(1) );
		model.addAttribute("idSesion", idSesion);

		// Recuperamos los stickers
		ArrayList<String> info_stickers = new TFMControladorCargaArchivos().carga_archivos_servidor("classpath:/static/imagenes/stickers/*");
		model.addAttribute("misStickers", info_stickers);
		log.info("misStickers {}", info_stickers);

		// Se copian las fuentes del paquete a un directorio temporal
		TFMUtils.creaDirectorioFuentes();

		return "TFMprincipal";
	}

}
