package usal.jac.tfm;

import java.util.List;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class TFMPeticion {

    // private static final Logger logger =
    // LoggerFactory.getLogger(TFMControladorGeneraVideo.class);

    private String id;
    private String nombre;
    private String duracion;
    private String desde;
    private String hasta;
    private String precision;
    private List<TFMNota> notas;

    public String getNombre() {
        return nombre;
    }



    public String getPrecision() {
        return precision;
    }

    public void setPrecision(String precision) {
        this.precision = precision;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public String getHasta() {
        return hasta;
    }

    public void setHasta(String hasta) {
        this.hasta = hasta;
    }

    public String getDesde() {
        return desde;
    }

    public void setDesde(String desde) {
        this.desde = desde;
    }

    public List<TFMNota> getNotas() {
        return notas;
    }

    public void setNotas(List<TFMNota> notas) {
        this.notas = notas;
    }
}
