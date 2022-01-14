package usal.jac.tfm.TFMUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import io.netty.util.BooleanSupplier;


public class TFMInfoSesion {

    
    private String idSesionPrivada;
    private long milisMuerte;
    private static long _caduc = 100000; 


    // String idSesionNavegador;
    // Time finSesion

    private static final Logger logger = LoggerFactory.getLogger(TFMInfoSesion.class);

    /**
     * Constructor que genera un id de sesión para cada jsession (
     */
    public TFMInfoSesion(String caducidad) {
        // Se asigna un nuevo id privado
        idSesionPrivada = RandomStringUtils.randomAlphanumeric(50);
        logger.warn("TFMInfoSesion: Creado nuevo identificador de sesión privada : {}", idSesionPrivada);

        // Se actualiza el timeout
        long caduc = 1000000;

        try {
            if (caducidad.indexOf("s")>0) { caduc = Long.parseLong(caducidad.replaceAll ("s", "")); caduc = caduc * 60000; }
            else if (caducidad.indexOf("m")>0) { caduc = Long.parseLong(caducidad.replaceAll ("m", "")); caduc = caduc * 360000;}
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("TFMInfoSesion: error parseando milisegundos de sesión, se deja en "+ caduc);
        }

        _caduc = caduc;

        // Se indica el valor inicial del timeout.
        setMilisMuerte(System.currentTimeMillis() +caduc );

    }




    /**
     * Constructor que genera un id de sesión para cada jsession (
     */
    public TFMInfoSesion(String id_proyecto, String caducidad) {
        // Se asigna un nuevo id privado
        idSesionPrivada = id_proyecto;
        logger.warn("TFMInfoSesion: actualizando id de sesión con valor anterior : {}", idSesionPrivada);

        // Se actualiza el timeout
        long caduc = 1000000;

        try {
            if (caducidad.indexOf("s")>0) { caduc = Long.parseLong(caducidad.replaceAll ("s", "")); caduc = caduc * 60000; }
            else if (caducidad.indexOf("m")>0) { caduc = Long.parseLong(caducidad.replaceAll ("m", "")); caduc = caduc * 360000;}
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("TFMInfoSesion: error parseando milisegundos de sesión, se deja en "+ caduc);
        }

        _caduc = caduc;

        // Se indica el valor inicial del timeout.
        setMilisMuerte(System.currentTimeMillis() +caduc );

    }

    
    /** 
     * Método que devuelve el identificador privado de sesión de este objeto TFMInfoSesion
     * @return String Identificador interno de sesión, o -1 si la sesión ha caducado
     */
    public String getIDsesion() {

        // logger.info ("RECUPERANDO ID SESION");
        if (getMilisMuerte() < System.currentTimeMillis())
        {
            /* logger.info ("LA sesión MUERE por caducidad!!!"); */
            return "-1"; // Se devuelve un código de error
        }
        else
        {
            // Se actualiza el valor inicial del timeout.
            setMilisMuerte(System.currentTimeMillis() + _caduc );


            /* logger.info ("NO MUERE "+getMilisMuerte() ); */
            /* logger.info ("NO MUERE "+System.currentTimeMillis() ); */
        }

        return idSesionPrivada;
    }


        


    public long getMilisMuerte() {
        return milisMuerte;
    }

    public void setMilisMuerte(long milisMuerte) {
        this.milisMuerte = milisMuerte;
    }
}
