package usal.jac.tfm;

public class TFMNota {
    private String texto;
    private String posX;
    private String posY;
    private String estilo;
    private String tam;
    private String color;
    private String inicio;
    private String fin;
    private String img_nota; 
    
    
    /** 
     * @return String
     */
    public String getPosY() {
        return posY;
    }


    
    /** 
     * @param posY
     */
    public void setPosY(String posY) {
        this.posY = posY;
    }

    
    /** 
     * @return String
     */
    public String getPosX() {
        return posX;
    }

    
    /** 
     * @param posX
     */
    public void setPosX(String posX) {
        this.posX = posX;
    }

    
    /** 
     * @return String
     */
    public String getTexto() {
        return texto;
    }

    
    /** 
     * @param texto
     */
    public void setTexto(String texto) {
        this.texto = texto;
    }

    
    /** 
     * @return String
     */
    public String getEstilo() {
        return estilo;
    }

    
    /** 
     * @param estilo
     */
    public void setEstilo(String estilo) {
        this.estilo = estilo;
    }

    
    /** 
     * @return String
     */
    public String getTam() {
        return tam;
    }

    
    /** 
     * @param tam
     */
    public void setTam(String tam) {
        this.tam = tam;
    }

    
    /** 
     * @return String
     */
    public String getColor() {
        return color;
    }

    
    /** 
     * @param color
     */
    public void setColor(String color) {
        this.color = color;
    }

    
    /** 
     * @return String
     */
    public String getInicio() {
        return inicio;
    }

    
    /** 
     * @param inicio
     */
    public void setInicio(String inicio) {
        this.inicio = inicio;
    }

    
    /** 
     * @return String
     */
    public String getFin() {
        return fin;
    }

    
    /** 
     * @param fin
     */
    public void setFin(String fin) {
        this.fin = fin;
    }

    
    /** 
     * @return String
     */
    public String getImg_nota() {
        return img_nota;
    }

    
    /** 
     * @param img_nota
     */
    public void setImg_nota(String img_nota) {
        this.img_nota = img_nota;
    }


    /*
     * "inicio": inicio_nota - inicio_seccion,
     * "fin": fin_nota - fin_seccion,
     * "texto": document.getElementById("recuadro"+id_nota).value,
     * "posX": document.getElementById("NOTA_"+id_nota).style.left,
     * "posY": document.getElementById("NOTA_"+id_nota).style.top,
     * "estilo": document.getElementById("recuadro"+id_nota).classList,
     * "tam": document.getElementById("recuadro"+id_nota).style.fontSize,
     * "color": document.getElementById("recuadro"+id_nota).style.color
     */
}
