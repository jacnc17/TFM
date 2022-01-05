package usal.jac.tfm.TFMUtils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import usal.jac.tfm.TFMControladorGeneraVideo;

public class TFMUtilidadesConversion {

    private static final Logger logger = LoggerFactory.getLogger(TFMControladorGeneraVideo.class);

    /**
     * Método que convierte un archivo PNG a JPG.
     * 
     * @param origen  Archivo original PNG a convertir
     * @param destino Archivo destino a generar en formato JPG
     */
    public static void conviertePNG_GIFaJPG(Path origen, Path destino) {
        logger.info("Solicitada conversión de " + origen.toString() + " a " + destino.toString());
        try {
            //Path source = Paths.get("C:\\test\\javanullpointer.png");
            //Path target = Paths.get("C:\\test\\new.jpg");

            BufferedImage originalImage = ImageIO.read(origen.toFile());

            // jpg needs BufferedImage.TYPE_INT_RGB
            // png needs BufferedImage.TYPE_INT_ARGB

            // create a blank, RGB, same width and height
            BufferedImage newBufferedImage = new BufferedImage(
                    originalImage.getWidth(),
                    originalImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB);

            // draw a white background and puts the originalImage on it.
            newBufferedImage.createGraphics()
                    .drawImage(originalImage,
                            0,
                            0,
                            Color.WHITE,
                            null);

            // save an image
            ImageIO.write(newBufferedImage, "jpg", destino.toFile());
            logger.info ("Imagen generada correctamente");
        } catch (Exception e) {
            logger.error("Error en la conversión a JPG!");
            e.printStackTrace();
        }
    }


    
}
