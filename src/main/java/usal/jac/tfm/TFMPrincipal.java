package usal.jac.tfm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.WebUtils;

import usal.jac.tfm.TFMUtils.TFMConstantes;
import usal.jac.tfm.TFMUtils.TFMInfoSesion;
import usal.jac.tfm.TFMUtils.TFMUtils;


@Controller
@SpringBootApplication
public class TFMPrincipal {
	// @Value("${spring.web.resources.static-locations}")
	@Value("${file.dirTrabajo}")
	String directorioTrabajo;

	@Value("${file.uploadDir}")
	String dest;

	@Value("${server.servlet.session.timeout}")
	String caducidad;

	static boolean purgado = false;

	private static final Logger log = LoggerFactory.getLogger(TFMPrincipal.class);

	public static File directorio_temporal; 

	public static void main(String[] args) {
		SpringApplication.run(TFMPrincipal.class, args);

		log.info("ARRANCANDO APLICACIÓN!!!!!!!!!!!!!!!");

	}

	@Autowired
	private HttpServletRequest request;

	@GetMapping("/")
	/* public String inicio(Model model, @CookieValue("id_proyecto") String id_proyecto) throws IOException { */
	public String inicio(Model model) throws IOException {

		// Se borran los directorios temporales de la anterior ejecución -> evitamos saturar el servidor.
		if (!purgado)
		{
			log.info("_____________________________________ purgando {} ", directorioTrabajo + File.separator + dest);
			purgado = true;
			FileUtils.deleteDirectory (new File(directorioTrabajo + File.separator + dest));
		}



		String id_proyecto = "";
		
		if (WebUtils.getCookie(request, "id_proyecto") != null)
			id_proyecto = WebUtils.getCookie(request, "id_proyecto").getValue();

		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("id_proyecto"+id_proyecto);
		System.out.println("");
		System.out.println("");

		// OK String idSesion = RequestContextHolder.currentRequestAttributes().getSessionId(); // Recupera el id de sesión
		String idSesion = "";
		
		if (!TFMUtils.checkSesion(directorioTrabajo, dest, id_proyecto))
		{
			log.info("ARRANCANDO : se invalida sesión de usuario porque su directorio de trabajo no existía");
			id_proyecto = null;
		}

		if (id_proyecto == null || id_proyecto.equals("")) {

			idSesion = TFMConstantes.getInfoSesion(RequestContextHolder.currentRequestAttributes().getSessionId(),
					caducidad); // Recupera el id de sesión

			// Se comprueba si esta sesión está ya registrada
			if (idSesion == null) {

				// Si no lo estaba, se genera una nueva sesión. Para el usuario se manejará este id interno de sesión
				idSesion = TFMConstantes.anadeSesion(RequestContextHolder.currentRequestAttributes().getSessionId(),
						caducidad);

				log.info("ARRANCANDO CON NUEVO " + idSesion + ", en " + directorioTrabajo + File.separator + dest);

			} else {
				log.info("ARRANCANDO CON SESION ESTABLE " + idSesion + ", en " + directorioTrabajo + File.separator + dest);

			}
			
		} else {
			idSesion = id_proyecto; // Recuperamos el id de sesión anterior

			// Se actualiza en la tabla interna
			TFMConstantes.infoSesion.put(RequestContextHolder.currentRequestAttributes().getSessionId(), new TFMInfoSesion(id_proyecto, caducidad));

			log.info("ARRANCANDO CON SESION RECUPERADA " + idSesion + ", en " + directorioTrabajo + File.separator + dest);

		}

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

		// Creamos el directorio temporal
		directorio_temporal = Files.createTempDirectory("tmpDirPrefix").toFile();

		// https://www.baeldung.com/java-temp-directories
		// Nos aseguramos de que se borrará al finalizar la MV
		directorio_temporal.toPath().toFile().deleteOnExit();

		// Recuperamos los stickers
		ArrayList<String> info_stickers = new TFMControladorCargaArchivos().carga_archivos_servidor("classpath:/static/imagenes/stickers/*", directorio_temporal);
		model.addAttribute("misStickers", info_stickers);
		log.info("misStickers {}", info_stickers);

		// Se copian las fuentes del paquete a un directorio temporal
		TFMUtils.creaDirectorioFuentes(directorio_temporal);

		return "TFMprincipal";
	}

}


