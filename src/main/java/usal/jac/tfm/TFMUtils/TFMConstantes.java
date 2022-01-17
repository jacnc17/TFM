package usal.jac.tfm.TFMUtils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Clase que define constantes comunes a todo el proyecto
 */
public final class TFMConstantes {
    private static List<String> mimes_video = new ArrayList<String>();

    private static List<String> mimes_imagenes = new ArrayList<String>();
    private static List<String> mimes_JPG = new ArrayList<String>();

    private static List<String> mimes_GIF = new ArrayList<String>();
    private static List<String> mimes_PNG = new ArrayList<String>();

    public static final String dirMiniaturas = "miniaturas";

    public static Hashtable<String, TFMInfoSesion> infoSesion = new Hashtable<String, TFMInfoSesion>();

    public static int altoMiniatura = 300;

    /**
     * Método que se llamará al iniciar la aplicación
     */
    public static void inicializaConstantes() {

        // Imágenes JPEG
        mimes_JPG.add("image/jpeg");
        mimes_JPG.add("image/pjpeg");

        // Imágenes GIF
        mimes_GIF.add("image/gif");

        // Imágenes PNG
        mimes_PNG.add("image/png");

        // Colector de MIMES de imágenes
        mimes_imagenes.addAll(mimes_JPG);
        mimes_imagenes.addAll(mimes_GIF);
        mimes_imagenes.addAll(mimes_PNG);

        // Vídeos AVI
        mimes_video.add("application/x-troff-msvideo");
        mimes_video.add("video/avi");
        mimes_video.add("video/msvideo");
        mimes_video.add("video/x-msvideo");

        // Vídeos MP4
        mimes_video.add("video/mp4");

        // Vídeos MKV
        mimes_video.add("video/x-matroska");

    }

    public static List<String> getMimes_video() {
        if (mimes_video.size() == 0)
            inicializaConstantes(); // Inicializamos el singleton
        return mimes_video;
    }

    public static void setMimes_video(List<String> mimes_video) {
        TFMConstantes.mimes_video = mimes_video;
    }

    public static List<String> getMimes_imagenes() {
        return mimes_imagenes;
    }

    public static void setMimes_imagenes(List<String> mimes_imagenes) {
        TFMConstantes.mimes_imagenes = mimes_imagenes;
    }

    public static List<String> getMimes_JPG() {
        if (mimes_JPG.size() == 0)
            inicializaConstantes(); // Inicializamos el singleton
        return mimes_JPG;
    }

    public static void setMimes_JPG(List<String> mimes_JPG) {
        TFMConstantes.mimes_JPG = mimes_JPG;
    }

    public static List<String> getMimes_GIF() {
        if (mimes_GIF.size() == 0)
            inicializaConstantes(); // Inicializamos el singleton

        return mimes_GIF;
    }

    public static void setMimes_GIF(List<String> mimes_GIF) {
        TFMConstantes.mimes_GIF = mimes_GIF;
    }

    public static List<String> getMimes_PNG() {
        if (mimes_PNG.size() == 0)
            inicializaConstantes(); // Inicializamos el singleton
        return mimes_PNG;
    }

    public static void setMimes_PNG(List<String> mimes_PNG) {
        TFMConstantes.mimes_PNG = mimes_PNG;
    }

    public static String getDirminiaturas() {
        return dirMiniaturas;
    }

    /**
     * Recupera del singleton el identificador interno de sesión.
     */
    public static String getInfoSesion(String idSesionNavegador, String caducidad) {
        // System.out.println("getInfoSesion = " + idSesionNavegador);

        String resultado = null;

        TFMInfoSesion temp = infoSesion.get(idSesionNavegador);

        if (temp == null)
            resultado =  TFMConstantes.anadeSesion (idSesionNavegador, caducidad);
        else
            resultado = temp.getIDsesion();

        return resultado;
    }

    public static String anadeSesion(String idSesion, String caducidad) {
        TFMInfoSesion temporal = new TFMInfoSesion(caducidad);

        infoSesion.put(idSesion, temporal);
        return temporal.getIDsesion();
    }
}
