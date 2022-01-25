package usal.jac.tfm;

import java.io.IOException;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;

import usal.jac.tfm.TFMUtils.TFMConstantes;
import usal.jac.tfm.TFMUtils.TFMUtils;

@RestController

/**
 * Clase para atender las solicitudes de detención de un proceso de render.
*/
public class TFMControladorParaProceso {
    private static final Logger logger = LoggerFactory.getLogger(TFMControladorParaProceso.class);
    private static Vector<String> sesiones_canceladas = new Vector <String> ();

	@Value("${server.servlet.session.timeout}")
	String caducidad;
   
    /** 
     * Método para eliminar los contenidos de un proyecto del servidor, a petición del cliente.
     * @return String Resultado del borrado, considerando la posibilidad de que el directorio aún no se haya creado.
     * @throws IOException
     */
    @GetMapping("/paraProceso")
    public String para_proceso() throws IOException {
        // Definición de variables
        String idSesion = "";
        String resultado = "";

        try
        {
            idSesion = TFMConstantes.getInfoSesion(RequestContextHolder.currentRequestAttributes().getSessionId(), caducidad);

            Process proc = TFMUtils.recupera_proceso (idSesion);
            logger.info("para_proceso. Destruyendo proceso asociado y marcando sesión como bloqueada temporalmente." );
            proc.destroy();

            // Se registra la sesión como "finalizada" para que no lance ninguno de los procesos posteriores.
            setSesion_cancelada(idSesion);
        } 
        catch (Exception e) { resultado = "KO"; e.printStackTrace();  }

        return resultado;
    }

    /**
     * Método para recuperar las sesiones canceladas
    */
    public static Vector<String> getSesiones_canceladas() {
        return sesiones_canceladas;
    }


    /**
     * Método que invalida el proceso de render de una sesión por petición del usuario
    */
    public static void setSesion_cancelada (String id_sesion) {
        sesiones_canceladas.add (id_sesion);
    }

    /**
     * Método que determina si los renders de una sesión están temporalmente cancelados.
    */
    public static boolean isSesion_cancelada (String id_sesion) {
        return (sesiones_canceladas.indexOf(id_sesion) > -1);
    }


    /**
     * Método que permite nuevos renders para una sesión. Se llamará al final de proceso de renderizado.
    */
    public static void libera_sesion (String id_sesion) {
        sesiones_canceladas.remove(id_sesion);
    }
}