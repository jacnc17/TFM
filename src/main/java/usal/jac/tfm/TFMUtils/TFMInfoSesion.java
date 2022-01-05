package usal.jac.tfm.TFMUtils;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TFMInfoSesion {
    private String idSesionPrivada;
    // String idSesionNavegador;
    // Time finSesion

    private static final Logger logger = LoggerFactory.getLogger(TFMInfoSesion.class);

    /**
     * Constructor que genera un id de sesión para cada jsession (
     */
    public TFMInfoSesion() {

        idSesionPrivada = RandomStringUtils.randomAlphanumeric(50);
        logger.warn("TFMInfoSesion: Creado nuevo identificador de sesión privada : {}", idSesionPrivada);

    }

    /**
     * Método que devuelve el identificador privado de sesión de este objeto TFMInfoSesion
    */
    public String getIDsesion() {
        return idSesionPrivada;
    }
}
