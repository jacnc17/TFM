package usal.jac.tfm.TFMUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import usal.jac.tfm.excepciones.TFMExcepcionArchivo;

/**
 * Clase auxiliar que contendrá métodos trasversales para toda la aplicación
 */
public class TFMUtils {

    private static final Logger logger = LoggerFactory.getLogger(TFMUtils.class);
    public static String directorio_fuentes = "";
    private static Hashtable<String, String> avanceRenders = new Hashtable<String, String>();


    /** 
     * Método que determina si la sesión de un usuario sigue existiendo en el servidor.
     */
    public static boolean checkSesion(String directorioTrabajo, String dest, String id_proyecto) {
        boolean resultado = false; // Por defecto se indica que no existe

		Path fileStorageLocation = Paths.get(directorioTrabajo).toAbsolutePath().normalize();
		logger.info("checkSesion. Recuperado fileStorageLocation general= " + fileStorageLocation);

		String directorioDestino = TFMUtils.creaDirectorio(fileStorageLocation.toString(), dest);
		logger.info("checkSesion. Directorio sesión creado/recuperado correctamente  " + directorioDestino);

		String directorioDestinoSesion = TFMUtils.creaDirectorio(directorioDestino, id_proyecto);
		logger.info("checkSesion. Directorio destino creado/recuperado correctamente para esta sesión " + directorioDestinoSesion);

        File nuevoDirectorio = new File(directorioDestino, id_proyecto); // Directorio candidato a ser creado

        logger.info("checkSesion. nuevoDirectorio = {}", nuevoDirectorio.toString());

        // Si el directorio a crear existía
        if (nuevoDirectorio.exists())
            resultado = true; 

        return resultado;
    }


    /**
     * Método para crear un directorio de almacenaje en el servidor con la ruta que
     * se le pasa por parámetro
     * 
     * @param ruta
     * @param dir
     * @return El nombre del directorio creado (o el directorio si ya existía) o
     *         null.
     */
    public static String creaDirectorio(String ruta, String dir) {
        // Definición de variables
        boolean existia = false;
        boolean creado = false;
        String resultado = null;

        File nuevoDirectorio = new File(ruta, dir); // Directorio candidato a ser creado

        logger.info("creaDirectorio. nuevoDirectorio = {}", nuevoDirectorio.toString());

        // Si el directorio a crear no existía
        if (!nuevoDirectorio.exists())
            try {
                creado = nuevoDirectorio.mkdir(); // Se intenta crear el directorio
                logger.info("Directorio " + nuevoDirectorio.toString() + " creado correctamente? " + creado);

            } catch (SecurityException se) {
                se.printStackTrace();
                logger.error(
                        "A security manager exists and its java.lang.SecurityManager.checkWrite(java.lang.String) method does not permit the named directory to be created");
            }
        else // El directorio ya existía
        {
            existia = true; // Se considera resultado ok
            logger.info("El directorio " + nuevoDirectorio.toString() + " ya existía");
        }

        // Si se ha creado o ya existía, se actualiza el valor a devolver
        if (creado || existia)
            resultado = nuevoDirectorio.toString();

        return resultado;
    }

    /**
     * Método que devuelve true si el archivo indicado es JPEG, false si es de
     * cualquier otro tipo.
     * 
     * @return True si es un archivo de imagen JPEG, false si es de imagen.
     * @param rutaArchivo Path del archivo que queremos verificar.
     */
    public static boolean esJPG(Path rutaArchivo) {
        boolean resultado = false;

        // Se recuperará el MIME
        String mimeType = URLConnection.guessContentTypeFromName(rutaArchivo.toString());

        if (TFMUtils.es_MIME_jpg(mimeType))
            resultado = true;

        logger.warn("esJPG: mime type de {} = {}. Es JPG? {}", rutaArchivo.toString(), mimeType, resultado);

        return resultado;
    }

    /**
     * Método que devuelve true si el archivo indicado es imagen, false si es de
     * cualquier otro tipo.
     * 
     * @return True si es un archivo de imagen, false si es de imagen.
     * @param rutaArchivo Path del archivo que queremos verificar.
     */
    public static boolean esIMAGEN(Path rutaArchivo) {
        boolean resultado = false;

        // Se recuperará el MIME
        String mimeType = URLConnection.guessContentTypeFromName(rutaArchivo.toString());

        if (TFMUtils.es_MIME_imagen(mimeType))
            resultado = true;

        logger.warn("esIMAGEN: mime type de {} = {}. Es IMAGEN? {}", rutaArchivo.toString(), mimeType, resultado);

        return resultado;
    }

    /**
     * Método que devuelve true si el archivo indicado es de vídeo, false si es de
     * imagen, TFMExcepcionArchivo si no es ninguno de ellos.
     * 
     * @return True si es un archivo de vídeo, false si es de imagen.
     * @throws TFMExcepcionArchivo en caso de no ser ni vídeo ni imagen.
     */
    public static boolean esVideo(String nombreArchivo) throws TFMExcepcionArchivo {
        // Definición de variables
        boolean resultado = false; // Por defecto, consideramos que es una imagen

        // Se gestionará el mime type
        try {
            // Se recuperará el MIME
            String mimeType = URLConnection.guessContentTypeFromName(nombreArchivo);

            if (TFMUtils.es_MIME_video(mimeType))
                resultado = true;

            logger.info("esVideo: mime type de {} = {}. Es vídeo? {}", nombreArchivo, mimeType, resultado);

        } catch (Exception egen) {
            logger.error("esVideo: {}. Error capturando mime type!", nombreArchivo);
            throw new TFMExcepcionArchivo("Problema recuperando el mime type del archivo " + nombreArchivo);
        }

        return resultado;
    }

    /**
     * Método para obtener la extensión de un archivo.
     */
    public static String getExtension(String nombreArchivo) {
        String resultado = nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1).toLowerCase();
        logger.info("getExtension: extensión de " + nombreArchivo + " = " + resultado);

        return resultado;
    }

    /**
     * Método para borrar del servidor de aplicaciones el archivo que se pase por
     * parámetro
     */
    public static boolean borra_archivo(String nombreArchivo) {
        boolean resultado = false;

        resultado = FileUtils.deleteQuietly(FileUtils.getFile(nombreArchivo));

        if (resultado)
            logger.info("Archivo {} borrado correctamente", nombreArchivo);
        else
            logger.error("Error borrando archivo {}!!! ", nombreArchivo);

        return resultado;
    }

    /**
     * Método que, a partir de un archivo de vídeo, generará miniaturas para
     * mostrar en los navegadores. Una miniatura será tipo jpg y la otra tipo vídeo
     * en baja resolución para que pueda mostrarse/editarse correctamente
     */
    public static void generaMiniaturaVideo(MultipartFile archivo, String dirDestinoMiniatura, String rutaOriginal,
            String rutaFFMPEG) {

        // Definición de variables
        String nombre_archivo = archivo.getOriginalFilename().replace(' ', '_');
        String nombre_miniatura = dirDestinoMiniatura + File.separator + "thumb_" + archivo.getSize() + "_"
                + nombre_archivo
                + ".jpg";
        // logger.debug("{}, {}, {}, {}", archivo.toString(),
        // dirDestinoMiniatura,rutaOriginal, nombre_miniatura);
        // logger.debug("{}", TFMConstantes.rutaFFMPEG);

        // C:\Users\jac_n\Formacion\USAL\2021_2022\TFM\ffmpeg\bin\ffmpeg -ss 00:00:01 -i
        // MOV_1311.mp4 -frames:v 1 -q:v 2 thumb_out.jpg
        try {
            // Genera vídeo a partir de un texto
            Process p = new ProcessBuilder(
                    rutaFFMPEG,
                    "-ss",
                    "00:00:01",
                    "-i",
                    rutaOriginal,
                    "-frames:v",
                    "1",
                    "-q:v",
                    "2",
                    nombre_miniatura + ".tmp.jpg").start();

            String stderr = IOUtils.toString(p.getErrorStream(), Charset.defaultCharset());
            String stdout = IOUtils.toString(p.getInputStream(), Charset.defaultCharset());

            logger.debug("stderr =" + stderr);
            logger.debug("stdout = " + stdout);
            logger.error("generaMiniaturaVideo: generada captura de {} ", nombre_archivo);

            // Una vez generada la captura, se generará la miniatura y se borra la captura.
            BufferedImage thumbImg = Scalr.resize(ImageIO.read(new File(nombre_miniatura + ".tmp.jpg")),
                    Scalr.Method.AUTOMATIC,
                    Scalr.Mode.AUTOMATIC, TFMConstantes.altoMiniatura, Scalr.OP_ANTIALIAS);

            // Se guarda en el directorio indicado en TFMConstantes.dirMiniaturas
            ImageIO.write(
                    thumbImg,
                    "jpg", // Para los thumbnails de vídeo se fuerza jpg
                    new File(nombre_miniatura));

            // Se borra el tmp_
            TFMUtils.borra_archivo(nombre_miniatura + ".tmp.jpg");

            logger.info("generaMiniaturaVideo: Archivo {} creado correctamente.", nombre_miniatura);

            // Generación de vídeo reducido y en formato válido para chromium y firefox ->
            // no todos los navegadores aceptan todos los formatos, ni siquiera mp4
            // ffmpeg -i 20210829_165616_CumpleEsterJaime.mp4 -vf
            // "scale=480:360:force_original_aspect_ratio=decrease,pad=480:360:-1:-1:color=black"
            // 20210829_165616_CumpleEsterJaime_mini.mp4

            // Destino
            String ruta_destino_video_mini_tmp = dirDestinoMiniatura + File.separator + "mini_" + archivo.getSize()
                    + "_" + nombre_archivo + "_" + ".mp4";
            String ruta_destino_video_mini_final = dirDestinoMiniatura + File.separator + "mini_" + archivo.getSize()
                    + "_" + nombre_archivo + ".mp4";

            // Genera vídeo a partir de un texto
            Process p2 = new ProcessBuilder(
                    rutaFFMPEG,
                    "-y",
                    "-i",
                    rutaOriginal,
                    "-vf",
                    /* OKWINDOWS "\"scale=640:360:force_original_aspect_ratio=decrease,pad=640:360:-1:-1:color=black\"", */
                    "scale=640:360:force_original_aspect_ratio=decrease,pad=640:360:-1:-1:color=black",
                    ruta_destino_video_mini_tmp).start();

            stderr = IOUtils.toString(p2.getErrorStream(), Charset.defaultCharset());
            stdout = IOUtils.toString(p2.getInputStream(), Charset.defaultCharset());

            logger.debug("stderr =" + stderr);
            logger.debug("stdout = " + stdout);
            logger.error("generaMiniaturaVideo: generada miniatura mp4 de {} ", nombre_archivo);

            // Para evitar un falso positivo del archivo, se crea con un sufijo temporal "_"
            File file = new File(ruta_destino_video_mini_tmp);

            // File (or directory) with new name
            File file2 = new File(ruta_destino_video_mini_final);

            if (file2.exists())
                throw new java.io.IOException("file exists");

            // Rename file (or directory)
            boolean success = file.renameTo(file2);
            logger.info("Generado archivo final {} ? {}", ruta_destino_video_mini_final.toString(), success);

        } catch (IOException ioe) {
            logger.error("generaMiniaturaVideo: Error generando miniatura de {}: {}", nombre_miniatura,
                    ioe.toString());
            ioe.printStackTrace();
        }
    }

    /**
     * Método que, a partir de un archivo de vídeo, generará una miniatura para
     * mostrar por pantalla.
     */
    public static void generaMiniaturaImagen(MultipartFile archivo, String dirDestinoMiniatura) {
        // Definición de variables
        String nombre_archivo = archivo.getOriginalFilename().replace(' ', '_');

        try {

            // Se genera la miniatura
            BufferedImage thumbImg = Scalr.resize(ImageIO.read(archivo.getInputStream()),
                    Scalr.Method.AUTOMATIC,
                    Scalr.Mode.AUTOMATIC, TFMConstantes.altoMiniatura, Scalr.OP_ANTIALIAS);

            // Se guarda en el directorio indicado en TFMConstantes.dirMiniaturas con
            // prefijo "thumb_".
            ImageIO.write(
                    thumbImg,
                    TFMUtils.getExtension(nombre_archivo),
                    new File(dirDestinoMiniatura + File.separator + "thumb_" + archivo.getSize() + "_"
                            + nombre_archivo));

            logger.info("generaMiniaturaImagen: Archivo thumb_{} creado correctamente.", nombre_archivo);

        } catch (IOException ioe) {
            logger.error("generaMiniaturaImagen: Error generando miniatura de {}: {}", nombre_archivo,
                    ioe.toString());
            ioe.printStackTrace();
        }
    }

    /**
     * Método que indica si un mime type es de tipo JPG.
     * 
     * @param nombre_mimeType Nombre del mime a identificar
     * @return true si el mime del archivo es del tipo JPG, false en otro caso.
     */
    public static boolean es_MIME_jpg(String nombre_mimeType) {
        return ((TFMConstantes.getMimes_JPG().contains(nombre_mimeType)));
    }

    /**
     * Método que indica si un mime type es de tipo imagen.
     * 
     * @param nombre_mimeType Nombre del mime a identificar
     * @return true si el mime del archivo es del tipo JPG, false en otro caso.
     */
    public static boolean es_MIME_imagen(String nombre_mimeType) {
        return ((TFMConstantes.getMimes_imagenes().contains(nombre_mimeType)));
    }

    /**
     * Método que indica si un mime type es de tipo vídeo.
     * 
     * @param nombre_mimeType Nombre del mime a identificar
     * @return true si el mime del archivo es del tipo vídeo aceptado, false en otro
     *         caso.
     * 
     */
    public static boolean es_MIME_video(String nombre_mimeType) {
        return ((TFMConstantes.getMimes_video().contains(nombre_mimeType)));

    }

    /**
     * Método que creara un directorio temporal en el sistema y almacenará las
     * fuentes para que FFMPEG pueda acceder a ellas
     */
    public static String creaDirectorioFuentes(File directorio_temporal) {
        String propiedad = "";
        URL inputUrl;
        File dest;
        ;

        try {

            directorio_fuentes = directorio_temporal.getAbsolutePath();
            propiedad = System.getProperty("java.io.tmpdir");
            logger.info("Creado directorio temporal {}, propiedad en sistema {}", directorio_fuentes, propiedad);
            // assertThat(tmpdir).startsWith(tmpDirsLocation);

            // Lista de fuentes registradas en el sistema, que coincidirán con lo que se ofrece al usuario por web.
            // TODO: hacer dinámico
            String[] lista_fuentes = { "arial.ttf", "arialbi.ttf", "ariali.ttf", "ariblk.ttf", "cour.ttf", "courbd.ttf",
                    "courbi.ttf", "couri.ttf", "Lato-Black.ttf", "Lato-BlackItalic.ttf", "Lato-Italic.ttf",
                    "Lato-Regular.ttf", "SansationLight.ttf", "trebuc.ttf", "trebucbd.ttf", "trebucbi.ttf",
                    "trebucit.ttf", "verdana.ttf", "verdanab.ttf", "verdanai.ttf", "verdanaz.ttf" };

            // Recorremos el listado de fuentes
            for (int i = 0; i < lista_fuentes.length; i++) {

                inputUrl = TFMUtils.class.getResource("/static/fuentes/" + lista_fuentes[i]);
                dest = new File(directorio_fuentes + File.separator + lista_fuentes[i]);
                FileUtils.copyURLToFile(inputUrl, dest);
            }



            // FFMPEG espera el directorio de fuentes en formato UNIX, por lo que hay que transformarlo.
            if (directorio_fuentes.indexOf("\\") > 0) {
                directorio_fuentes = FilenameUtils.separatorsToUnix(directorio_fuentes);
            }

            // Si hay nombre de unidad, se quita
            if (directorio_fuentes.indexOf("/") > 0) {
                directorio_fuentes = directorio_fuentes.substring(directorio_fuentes.indexOf("/"));
            }

            // Se añade un último separador para permitir anexar el archivo directamente.
            directorio_fuentes = directorio_fuentes + "/";

            logger.debug("directorio_fuentes para FFMPEG {}", directorio_fuentes);

        } catch (Exception e) {
            logger.error("Error creando directorio temporal!!!!");
            e.printStackTrace();
        }
        return directorio_fuentes;
    }

    /**
     * If we want to list all the files in the directory and skip further digging
     * into sub-directories, we can simply use java.io.File#listFiles:
     */
/*     public static Set<String> listFilesUsingJavaIO(String dir) {
        return Stream.of(new File(dir).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toSet());
    } */



    /**
     * Método para establecer el % de avance en una operación de render.
    */
    public static void setAvanceOperacion (String id_sesion, String porcentaje)
    {
        avanceRenders.put(id_sesion, porcentaje);
    }

    /**
     * Método para recuperar el % de avance en una operación de render.
    */
    public static String getAvanceOperacion (String id_sesion)
    {
        return avanceRenders.get(id_sesion);
    }


}
