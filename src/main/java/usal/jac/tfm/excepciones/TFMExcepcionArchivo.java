package usal.jac.tfm.excepciones;


public class TFMExcepcionArchivo extends RuntimeException {

	public TFMExcepcionArchivo(String msj) {
		super(msj);
	}

	public TFMExcepcionArchivo(String msj, Throwable motivo) {
		super(msj, motivo);
	}
}
