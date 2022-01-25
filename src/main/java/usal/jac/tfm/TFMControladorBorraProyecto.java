package usal.jac.tfm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;

import usal.jac.tfm.TFMUtils.TFMConstantes;

@RestController

public class TFMControladorBorraProyecto {
    @Value("${file.dirTrabajo}")
    String directorioTrabajo;

    @Value("${file.uploadDir}")
    String dest;

    @Value("${server.servlet.session.timeout}")
    String caducidad;

    @Value("${file.rutaFFPROBRE}")
    String rutaFFPROBRE;

    private static final Logger logger = LoggerFactory.getLogger(TFMControladorBorraProyecto.class);


    

    
    /** 
     * Método para eliminar los contenidos de un proyecto del servidor, a petición del cliente.
     * @return String Resultado del borrado, considerando la posibilidad de que el directorio aún no se haya creado.
     * @throws IOException
     */
    @GetMapping("/borraProyecto")
    public String borra_proyecto() throws IOException {
        // Definición de variables
        String idSesion = "";
        String resultado = ""; // La cadena vacía será éxito. Si no, es el mensaje a mostrar.

        try
        {
            idSesion = TFMConstantes.getInfoSesion(RequestContextHolder.currentRequestAttributes().getSessionId(), caducidad);
            logger.info("Recuperando directorio de {}", idSesion);

            // Nombre del directorio exclusivo de la sesión.
            Path destinationFile = Paths
                    .get(directorioTrabajo + File.separator + dest + File.separator + idSesion)
                    .toAbsolutePath().normalize();

            // Se borran los contenidos, dejando el directorio intacto
            logger.info("Se borran los contenidos, dejando {} intacto ", destinationFile.toString());

            FileUtils.cleanDirectory(destinationFile.toFile());
        } 
        catch (IllegalStateException iee) { iee.printStackTrace(); resultado = "¡Error capturando la sesión del usuario! Pulsa F5 e inténtalo de nuevo."; }
        catch (NullPointerException npe) { npe.printStackTrace(); resultado = "¡Error capturando el directorio del usuario! Pulsa F5 e inténtalo de nuevo."; }
        catch (IllegalArgumentException iae) { iae.printStackTrace(); if (iae.toString().indexOf("does not exist")==0)  resultado = "¡Error capturando el directorio del usuario! Pulsa F5 e inténtalo de nuevo."; }
        catch (IOException ioe) { ioe.printStackTrace(); resultado = "¡No pudieron borrarse los archivos! Inténtalo de nuevo en unos instantes"; }

        return resultado;
    }
}