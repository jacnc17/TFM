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
    
    public String getPosY() {
        return posY;
    }


    public void setPosY(String posY) {
        this.posY = posY;
    }

    public String getPosX() {
        return posX;
    }

    public void setPosX(String posX) {
        this.posX = posX;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getEstilo() {
        return estilo;
    }

    public void setEstilo(String estilo) {
        this.estilo = estilo;
    }

    public String getTam() {
        return tam;
    }

    public void setTam(String tam) {
        this.tam = tam;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getInicio() {
        return inicio;
    }

    public void setInicio(String inicio) {
        this.inicio = inicio;
    }

    public String getFin() {
        return fin;
    }

    public void setFin(String fin) {
        this.fin = fin;
    }

    public String getImg_nota() {
        return img_nota;
    }

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
