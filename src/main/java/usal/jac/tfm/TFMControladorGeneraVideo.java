package usal.jac.tfm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Vector;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;

import usal.jac.tfm.TFMUtils.TFMConstantes;
import usal.jac.tfm.TFMUtils.TFMUtils;

@RestController
@RequestMapping(value = "/creaVid", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.MULTIPART_FORM_DATA_VALUE)
public class TFMControladorGeneraVideo {
	@Value("${file.dirTrabajo}")
	String directorioTrabajo;

	@Value("${file.uploadDir}")
	String dest;

	@Value("${file.fuente}")
	String fuente;

	@Value("${file.rutaFFMPEG}")
	String rutaFFMPEG;

	private static final Logger logger = LoggerFactory.getLogger(TFMControladorGeneraVideo.class);

	private int totalArchivos = 0;

	/**
	 * Método principal que lanza la petición de generación de vídeo y responde con
	 * un array de bytes.
	 */
	@RequestMapping
	public byte[] genera(@RequestBody List<TFMPeticion> peticion) throws IOException {
		// Definición de variables
		String idSesion = TFMConstantes.getInfoSesion(RequestContextHolder.currentRequestAttributes().getSessionId());
		Process p; // Proceso del sistema operativo
		InputStream in = null; // Stream de bytes que será devuelto.
		float avance = 10f; // Porcentaje de avance en el render
		NumberFormat formateador = new DecimalFormat("0"); // 

		logger.info("GENERANDO directorio de destino para " + idSesion);

		Path fileStorageLocation = Paths.get(directorioTrabajo).toAbsolutePath().normalize();
		logger.info("Recuperado fileStorageLocation general= " + fileStorageLocation);

		String directorioDestino = TFMUtils.creaDirectorio(fileStorageLocation.toString(), dest);
		logger.info("Directorio sesión creado/recuperado correctamente  " + directorioDestino);

		String directorioDestinoSesion = TFMUtils.creaDirectorio(directorioDestino, idSesion);
		logger.info("Directorio destino creado/recuperado correctamente para esta sesión " + directorioDestinoSesion);

		String directorioDestinoVideo = TFMUtils.creaDirectorio(directorioDestinoSesion, "videoGenerado");
		logger.info("Directorio destino del vídeo creado/recuperado correctamente para esta sesión "
				+ directorioDestinoVideo);

		String cadenaConcat;

		// Recupera la secuencia de archivos a lanzar.
		setTotalArchivos(0); // Se inicializa el total de archivos
		Vector<List<String>> comandos = preparaSecuenciaArchivos(peticion, directorioDestinoSesion,
				directorioDestinoVideo);

		String stderr = "";
		String stdout = "";

		// Establecemos el proceso como iniciado (10%)
		TFMUtils.setAvanceOperacion(idSesion, formateador.format(avance));

		// Recorremos la petición
		for (int i = 0; i < comandos.size(); i++) {

			// // Genera vídeo a partir de un texto
			p = new ProcessBuilder(comandos.elementAt(i)).start();

			stderr = IOUtils.toString(p.getErrorStream(), Charset.defaultCharset());
			stdout = IOUtils.toString(p.getInputStream(), Charset.defaultCharset());

			logger.debug("stderr {}", stderr);
			logger.debug("stdout {} ", stdout);

			// En esta sección se avanzará hasta el 30%
			avance = avance + (40f / comandos.size());
			logger.debug ("avance en la generación de vídeo: {}", avance);
			TFMUtils.setAvanceOperacion(idSesion, formateador.format(avance) ) ;

		}

		// Establecemos el porcentaje de avance del proceso
		avance = 50f;
		TFMUtils.setAvanceOperacion(idSesion, formateador.format(avance));

		cadenaConcat = generaCadenaConcat(directorioDestinoVideo);
		// cadenaConcat = cadenaConcat.concat("\"");
		logger.info("cadenaConcat = {} ", cadenaConcat);

		// Ruta del archivo de destino: se llamará al archivo igual que el ID de sesión.
		// TODO: configurar archivo de destino en la aplicación de usuario.
		Path archivo_intermedio = Paths.get(directorioDestinoVideo + File.separator + "dummy.mp4").toAbsolutePath()
				.normalize();
		Path destinationFile = Paths.get(directorioDestinoVideo + File.separator + idSesion + ".mp4").toAbsolutePath()
				.normalize();
		logger.info("Archivo destino = {} ", destinationFile);

		// Establecemos el porcentaje de avance del proceso
		TFMUtils.setAvanceOperacion(idSesion, "60");
		
		// Se lanza el proceso FFMPEG
		p = new ProcessBuilder(Arrays.asList(rutaFFMPEG, "-y", "-i", cadenaConcat, "-vf",
				"\"scale=1280:720:force_original_aspect_ratio=decrease,pad=1280:720:-1:-1:color=black"
						+ "\"",
				"-bsf:a", "aac_adtstoasc", "-movflags", "faststart", "-fflags", "+discardcorrupt",
				"-f", "mp4", "-threads", "8", archivo_intermedio.toString())).start();

		stderr = IOUtils.toString(p.getErrorStream(), Charset.defaultCharset());
		stdout = IOUtils.toString(p.getInputStream(), Charset.defaultCharset());
		logger.debug("stderr {}", stderr);
		logger.debug("stdout {} ", stdout);


		// Recuperamos las notas
		String cadena_notas = generaCadenaNotas(((TFMPeticion) peticion.get(0)).getNotas(),
				((TFMPeticion) peticion.get(0)).getPrecision());

		logger.info("Cadena de notas recuperadas = {}", cadena_notas);

		// Si no hay notas, el proceso ha terminado.
		if (cadena_notas == null || cadena_notas.equals("")) {
			// Establecemos el porcentaje de avance del proceso
			TFMUtils.setAvanceOperacion(idSesion, "90");
			
			// ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			in = new FileInputStream(archivo_intermedio.toString());


		} //
		else // Hay notas
		{
			// Recuperamos los posibles archivos implicados en las notas.
			Vector<String> archivos_notas = generaCadenaArchivosNotas(directorioDestinoSesion,
					((TFMPeticion) peticion.get(0)).getNotas());

			// Se crea una lista dinámicamente con los posibles elementos de la incrustación
			// de notas
			// Recuperamos los archivos implicados en la notas. Si son notas de texto no
			// incluirán archivos,
			// pero si son archivos de imagen hay que hacer referencia a ellos.
			String[] lista_parametros = genera_lista_parametros_notas(archivo_intermedio.toString(),
					destinationFile.toString(), archivos_notas, cadena_notas);

			// Se lanza el proceso FFMPEG para la superposición de notas
			/*
			 * p = new ProcessBuilder(Arrays.asList(rutaFFMPEG, "-y", "-i", "dum.mp4",
			 * cadenaConcat, "-vf",
			 * "\"scale=1280:720:force_original_aspect_ratio=decrease,pad=1280:720:-1:-1:color=black"
			 * + "\"",
			 * "-bsf:a", "aac_adtstoasc", "-movflags", "faststart", "-fflags",
			 * "+discardcorrupt",
			 * "-f", "mp4", "-threads", "8", destinationFile.toString())).start();
			 */
			// Establecemos el porcentaje de avance del proceso
			TFMUtils.setAvanceOperacion(idSesion, "75");
			p = new ProcessBuilder(Arrays.asList(lista_parametros)).start();

			stderr = IOUtils.toString(p.getErrorStream(), Charset.defaultCharset());
			stdout = IOUtils.toString(p.getInputStream(), Charset.defaultCharset());
			logger.debug("stderr {}", stderr);
			logger.debug("stdout {} ", stdout);

			// Establecemos el porcentaje de avance del proceso
			TFMUtils.setAvanceOperacion(idSesion, "90");

			// ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			in = new FileInputStream(destinationFile.toString());
		}
		// Establecemos el porcentaje de avance del proceso
		TFMUtils.setAvanceOperacion(idSesion, "100");
		logger.info("Devolviendo array de bytes de {} bytes", in.available());

		return IOUtils.toByteArray(in);
	}

	/**
	 * Genera un array con los parámetros necesarios para la concatenación de notas,
	 * ya sean de texto o con imágenes.
	 */
	private Vector<String> generaCadenaArchivosNotas(String directorioSesion, List<TFMNota> notas) {
		// Definición de variables
		Vector<String> resultado = new Vector<String>();
		String tmp_img_nota = "";

		for (int n = 0; n < notas.size(); n++) {
			// Gestión del texto de la nota.
			tmp_img_nota = notas.get(n).getImg_nota();

			// Si la nota es una imagen incrustada
			if (tmp_img_nota != null && !(tmp_img_nota.trim()).equals("")) {
				String ruta = directorioSesion + File.separator + tmp_img_nota;

				// Comprobar que existe el archivo.
				// Si existe en este punto, era un archivo subido por el usuario o ya se había añadido antes.
				if (Files.exists(Paths.get(ruta))) {
					// Se añade al vector de resultado, incluyendo la ruta
					resultado.add(ruta);
				} else // El archivo no se encuentra en el directorio de la sesión.
				{
					try {

						// Recuperamos la lista de archivos en el directorio de stickers
						ClassLoader loader = this.getClass().getClassLoader();
						ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(loader);
						final Resource[] resources = resolver
								.getResources("classpath:/static/imagenes/stickers/" + tmp_img_nota);

						// Recorremos el listado de recursos.
						for (final Resource archivo : resources) {
							logger.info("carga_archivos_servidor: recuperado '{}'", archivo.getFilename());
							FileUtils.copyToDirectory(new File(archivo.getURI()),
									new File(Paths.get(directorioSesion).toUri()));
							logger.info("carga_archivos_servidor: copiado a directorio {}", directorioSesion);

							resultado.add(directorioSesion + File.separator + tmp_img_nota);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				}

			}
		}

		logger.debug("Archivos incrustados en forma de notas: " + resultado.toString());

		return resultado;
	}

	/***
	 * Genera un array con los parámetros necesarios para la concatenación de notas,
	 * ya sean de texto o con imágenes.
	 */
	private String[] genera_lista_parametros_notas(String archivo_intermedio, String archivo_destino,
			Vector<String> lista_archivos, String cadena_notas) {
		Vector<String> tmp = new Vector<String>();

		tmp.add(rutaFFMPEG);
		tmp.add("-y");
		tmp.add("-i");
		tmp.add(archivo_intermedio);

		for (int i = 0; i < lista_archivos.size(); i++) {
			tmp.add("-i");
			tmp.add(lista_archivos.elementAt(i));
		}

		tmp.add("-filter_complex");
		tmp.add("\"" + cadena_notas + "\"");
		tmp.add("-pix_fmt");
		tmp.add("yuv420p");
		tmp.add("-c:a");
		tmp.add("copy");
		tmp.add(archivo_destino);

		// -pix_fmt yuv420p %options% %options3% -c:a copy

		logger.debug("Cadena de inserción de archivos : {}", tmp.toString());

		/*
		 * ffmpeg -y -i 20210918_164036_CumpleJudith.mp4 -i imagen1.jpg -i
		 * IMG_20210717_213139.jpg -filter_complex "
		 * [1:v]scale2ref=(8/6)*ih/5/sar:ih/5[wm][outv];[outv][wm]overlay=20:20,
		 * drawtext=fontfile=/Users/jac_n/AppData/Local/Temp/
		 * tmpDirPrefix8568426333782101746/arial.ttf:text='NOTA
		 * 1':fontcolor=0xffffff:fontsize=80:x=0:y=0:enable='between(t,7.23,16.46)',
		 * drawtext=fontfile=/Users/jac_n/AppData/Local/Temp/
		 * tmpDirPrefix8568426333782101746/verdanaz.ttf:text='NOTA
		 * 2':fontcolor=0x9DA2F1:fontsize=100:x=174.6666:y=306.0:enable='between(t,2.24,
		 * 1500)',
		 * [2:v]scale2ref=(8/6)*ih/4/sar:ih/4[wm][outv];[outv][wm]overlay=400:400:enable
		 * ='between(t,5,15)'" -pix_fmt yuv420p %options% %options3% -c:a copy tmp88.mp4
		 */

		/*
		 * p = new ProcessBuilder(Arrays.asList(rutaFFMPEG, "-y", "-i", "dum.mp4",
		 * cadenaConcat, "-vf",
		 * "\"scale=1280:720:force_original_aspect_ratio=decrease,pad=1280:720:-1:-1:color=black"
		 * + "\"",
		 * "-bsf:a", "aac_adtstoasc", "-movflags", "faststart", "-fflags",
		 * "+discardcorrupt",
		 * "-f", "mp4", "-threads", "8", destinationFile.toString())).start();
		 */

		return tmp.toArray(new String[tmp.size()]);
	}

	/**
	 * Método que ante una cadena de notas genera la sentencia FFMPEG adecuada.
	 */
	private String generaCadenaNotas(List<TFMNota> notas, String precision) {
		String resultado = "";

		logger.debug("nota = {}", notas);

		if (notas != null) {
			// Definición de variables
			String texto = "";
			String posX = "";
			String posY = "";
			String duracion = "";
			String color = "";
			String hex = ""; // Color en formato hexadecimal
			String tam = ""; // Tamaño de fuente
			String estilo = ""; // Tipo de fuente y estilos
			String fichero_fuente = "";
			String inicio = ""; // Tiempo de inicio de una nota
			String fin = ""; // Tiempo de inicio de una nota
			String img_nota = ""; // En caso de que la nota sea una imagen, se incluye la URL completa.

			int colorR = 255;
			int colorG = 255;
			int colorB = 255;

			int total_imagenes_notas = 1; // En la sentencia FFMPEG, el i0 será siempre el vídeo sobre el que se  incrustará la imagen.

			double tiempo_inicio = 0;
			double tiempo_fin = 0;

			// Se recorre el detalle de las notas (incluido en la primera nota)
			for (int n = 0; n < notas.size(); n++) {
				// Gestión del texto de la nota.
				texto = notas.get(n).getTexto();
				img_nota = notas.get(n).getImg_nota();

				///////////// Bloque de gestión de intervalos de tiempo. Ejemplo:
				///////////// inicio":550,"fin":1000 -> enable='between(t,5.5,10)'
				inicio = notas.get(n).getInicio();
				fin = notas.get(n).getFin();
				logger.info("Recuperado inicio {} y fin {} para la nota {}, con precision {}", inicio, fin, n,
						precision);
				try {
					tiempo_inicio = Double.parseDouble(inicio) / Double.parseDouble(precision);
				} catch (Exception e) {
					e.printStackTrace();
					tiempo_fin = -1;
				}
				try {
					tiempo_fin = Double.parseDouble(fin) / Double.parseDouble(precision);
				} catch (Exception e) {
					e.printStackTrace();
					tiempo_fin = -1;
				}

				// Si hubo algún error, no habrá nota.
				if (tiempo_fin != -1) {
					duracion = ":enable='between(t," + tiempo_inicio + "," + tiempo_fin + ")'";
				}
				logger.info("Compuesto rango de duración: {} ", duracion);

				// Si la nota es una imagen incrustada
				if (img_nota != null && !(img_nota.trim()).equals("")) {

					logger.info("La nota {} es una imagen incrustada", n);
					////////////// Gestión de la posición de la nota
					posX = notas.get(n).getPosX();
					posY = notas.get(n).getPosY();

					posX = posX.replace("px", "");
					posY = posY.replace("px", "");

					// Se consideran dimensiones originales de 640 x 360. Se multiplican por 1.5 para llegar a 1280 x 720
					posX = "" + (10 + (Float.parseFloat(posX) * 1.538461538)); // Relación entre el vídeo final 1280x720 y la versión de edición 832x468
					posY = "" + (45 + (Float.parseFloat(posY) * 1.538461538)); // Relación entre el vídeo final 1280x720 y la versión de edición 832x468 MÁS el alto de la barra

					// [1:v]scale2ref=(8/6)*ih/5/sar:ih/5[wm][outv];[outv][wm]overlay=20:20, Se recupera el tamaño de la imagen original, que viene en formato anchoxalto
					// (ej 800x600)
					int ancho = Integer
							.parseInt(notas.get(n).getTam().substring(0, notas.get(n).getTam().indexOf("x")));
					int alto = Integer
							.parseInt(notas.get(n).getTam().substring(notas.get(n).getTam().indexOf("x") + 1));
					int max_alto = Integer.parseInt(notas.get(n).getEstilo()); // Si la nota es una imagen, se indica en "estilo" el alto máximo en pantalla
																				// -> hay que actualizarlo para el vídeo

					// Multiplicador para obtener el ancho adaptado.
					double proporcion = (double) ancho / alto;

					logger.debug(
							"La nota {} tiene dimensiones {} de ancho x {} de alto, con {} alto máximo original (proporción {} )",
							n, ancho, alto, max_alto, proporcion);
					logger.debug("La nota {} está en {},{}", n, posX, posY);

					// Si no es la primera nota, se anexa una coma.
					if (n > 0)
						resultado = resultado + ",";

					resultado = resultado + "[" + (total_imagenes_notas) + ":v]scale2ref="
							+ ((float) ancho / alto) * 1.538461538 * max_alto + "/sar:" + (1.538461538 * max_alto);
					resultado = resultado + "[wm][outv];[outv][wm]overlay=" + posX + ":" + posY;
					resultado = resultado + duracion;

					logger.info("Cadena compuesta para la nota {}: {}", n, resultado);

					// Se incrementa el número de notas que incluyen imágenes
					total_imagenes_notas++;

				} //
				else if (texto == null || (texto.trim()).equals("")) // Si no hay texto, se ignora el resto y se pasa // a la siguiente nota
				{
					logger.info("La nota {} no posee contenido válido, se ignora", n);
				} // 
				else // Hay texto a escribir.
				{
					////////////// Gestión de la posición de la nota
					posX = notas.get(n).getPosX();
					posY = notas.get(n).getPosY();

					posX = posX.replace("px", "");
					posY = posY.replace("px", "");

					// Se consideran dimensiones originales de 640 x 360. Se multiplican por 1.5 para llegar a 1280 x 720
					// Es necesario realizar un ajuste en función del alto renderizado. El motivo es que el 0,0 para ffmpeg difiere en función de 
					// si es una línea con caracteres "bajos" tipo aeom, o con caracteres "altos" tipo dbtli . Por eso hay que hacer un ajuste adicional
					posX = "" + (10 + (Float.parseFloat(posX) * 1.538461538)); // Relación entre el vídeo final 1280x720 y la versión de edición 832x468

					////////////////// Bloque de gestión del tamaño de fuente y posición en el eje y (vertical)
					tam = notas.get(n).getTam();
					if (tam == null || tam.equals("") || tam.equals("1em")) {
						tam = "20";
						posY = "" + ((60 + (Float.parseFloat(posY)) * 1.538461538)) + "-th"; // Relación entre el vídeo final 1280x720 y la versión de edición 832x468 MÁS el alto de la barra

					} else if (tam.equals("3em")) {
						tam = "63";
						posY = "" + ((115 + (Float.parseFloat(posY)) * 1.538461538)) + "-th"; // Relación entre el vídeo final 1280x720 y la versión de edición 832x468 MÁS el alto de la barra

					} else if (tam.equals("6em")) {
						tam = "136";
						posY = "" + ((190 + (Float.parseFloat(posY)) * 1.538461538)) + "-th"; // Relación entre el vídeo final 1280x720 y la versión de edición 832x468 MÁS el alto de la barra
					}

					///////////// Bloque de gestión de estilos
					estilo = notas.get(n).getEstilo();
					logger.info("La nota {} posee fuente {}.", n, estilo);

					if (estilo == null || estilo.trim().equals(""))
						fichero_fuente = "";
					else
						estilo = estilo.toLowerCase();

					if (estilo.indexOf("arial") > -1 && estilo.indexOf("bold") > -1 && estilo.indexOf("italic") > -1)
						fichero_fuente = TFMUtils.directorio_fuentes + "arialbi.ttf";
					else if (estilo.indexOf("arial") > -1 && estilo.indexOf("bold") > -1)
						fichero_fuente = TFMUtils.directorio_fuentes + "ariblk.ttf";
					else if (estilo.indexOf("arial") > -1 && estilo.indexOf("italic") > -1)
						fichero_fuente = TFMUtils.directorio_fuentes + "ariali.ttf";
					else if (estilo.indexOf("arial") > -1)
						fichero_fuente = TFMUtils.directorio_fuentes + "arial.ttf";
					else if (estilo.indexOf("courier") > -1 && estilo.indexOf("bold") > -1
							&& estilo.indexOf("italic") > -1)
						fichero_fuente = TFMUtils.directorio_fuentes + "courbi.ttf";
					else if (estilo.indexOf("courier") > -1 && estilo.indexOf("bold") > -1)
						fichero_fuente = TFMUtils.directorio_fuentes + "courbd.ttf";
					else if (estilo.indexOf("courier") > -1 && estilo.indexOf("italic") > -1)
						fichero_fuente = TFMUtils.directorio_fuentes + "couri.ttf";
					else if (estilo.indexOf("courier") > -1)
						fichero_fuente = TFMUtils.directorio_fuentes + "cour.ttf";
					else if (estilo.indexOf("lato") > -1 && estilo.indexOf("bold") > -1
							&& estilo.indexOf("italic") > -1)
						fichero_fuente = TFMUtils.directorio_fuentes + "Lato-BlackItalic.ttf";
					else if (estilo.indexOf("lato") > -1 && estilo.indexOf("bold") > -1)
						fichero_fuente = TFMUtils.directorio_fuentes + "Lato-Black.ttf";
					else if (estilo.indexOf("lato") > -1 && estilo.indexOf("italic") > -1)
						fichero_fuente = TFMUtils.directorio_fuentes + "Lato-Italic.ttf";
					else if (estilo.indexOf("lato") > -1)
						fichero_fuente = TFMUtils.directorio_fuentes + "Lato-Regular.ttf";
					else if (estilo.indexOf("trebuchet") > -1 && estilo.indexOf("bold") > -1
							&& estilo.indexOf("italic") > -1)
						fichero_fuente = TFMUtils.directorio_fuentes + "trebucbi.ttf";
					else if (estilo.indexOf("trebuchet") > -1 && estilo.indexOf("bold") > -1)
						fichero_fuente = TFMUtils.directorio_fuentes + "trebucbd.ttf";
					else if (estilo.indexOf("trebuchet") > -1 && estilo.indexOf("italic") > -1)
						fichero_fuente = TFMUtils.directorio_fuentes + "trebucit.ttf";
					else if (estilo.indexOf("trebuchet") > -1)
						fichero_fuente = TFMUtils.directorio_fuentes + "trebuc.ttf";
					else if (estilo.indexOf("verdana") > -1 && estilo.indexOf("bold") > -1
							&& estilo.indexOf("italic") > -1)
						fichero_fuente = TFMUtils.directorio_fuentes + "verdanaz.ttf";
					else if (estilo.indexOf("verdana") > -1 && estilo.indexOf("bold") > -1)
						fichero_fuente = TFMUtils.directorio_fuentes + "verdanab.ttf";
					else if (estilo.indexOf("verdana") > -1 && estilo.indexOf("italic") > -1)
						fichero_fuente = TFMUtils.directorio_fuentes + "verdanai.ttf";
					else if (estilo.indexOf("verdana") > -1)
						fichero_fuente = TFMUtils.directorio_fuentes + "verdana.ttf";
					else if (estilo.indexOf("bold") > -1 && estilo.indexOf("italic") > -1)
						fichero_fuente = TFMUtils.directorio_fuentes + "arialbi.ttf";
					else if (estilo.indexOf("bold") > -1)
						fichero_fuente = TFMUtils.directorio_fuentes + "ariblk.ttf";
					else if (estilo.indexOf("italic") > -1)
						fichero_fuente = TFMUtils.directorio_fuentes + "ariali.ttf";
					else
						fichero_fuente = TFMUtils.directorio_fuentes + "arial.ttf";

					logger.info("Fuente de texto para la nota {} = {}", n, fichero_fuente);

					///////////// Bloque de gestión del color de nota
					// It can be the name of a color as defined below (case insensitive match) or a
					// [0x|#]RRGGBB[AA] sequence, possibly followed by @ and a string representing
					// the alpha component: 0xF0F8FF
					// Hay que transformar rgb(139, 50, 50) a 0xF0F8FF
					color = notas.get(n).getColor();

					// Por defecto, en color blanco.
					hex = "0xffffff";

					try {

						// Se procesa la cadena con formato rgb(), ejemplo rgb(139, 50, 50)
						colorR = Integer.parseInt((color.split(",")[0]).split("\\(")[1].trim());
						colorG = Integer.parseInt(color.split(",")[1].trim());
						colorB = Integer.parseInt((color.split(",")[2]).split("\\)")[0].trim());
						logger.debug("preparaSecuenciaArchivos. Color original {}, rojo {}, verde {}, azul {}", color,
								colorR, colorG, colorB);
						hex = String.format("0x%02X%02X%02X", colorR, colorG, colorB);

					} catch (ArrayIndexOutOfBoundsException aiobe) {
						logger.debug("Error al procesar los colores, se deja en blanco");
					} catch (NumberFormatException nfe) {
						logger.debug("Error al procesar los colores, se deja en blanco");
					} catch (PatternSyntaxException pse) {
						logger.debug("Error al procesar los colores, se deja en blanco");
					} catch (NullPointerException npe) {
						logger.debug("Error al procesar los colores, se deja en blanco");
					} catch (IllegalFormatException ife) {
						logger.debug("Error al procesar los colores, se deja en blanco");
					}

					logger.info("preparaSecuenciaArchivos.        Color hex {}", hex);
					logger.info("preparaSecuenciaArchivos: notas.texto {}, posX {}, posY {}", texto, posX, posY);

					// ffmpeg -y -i fichero.mp4 -vf
					// "drawtext=fontfile=/Users/jac_n/Formacion/USAL/2021_2022/Lato/Lato-Black.ttf:text='ASDF':fontcolor=blue:fontsize=200:x="+posX+":y=110,drawtext=fontfile=/Users/jac_n/Formacion/USAL/2021_2022/Lato/Lato-Black.ttf:text='Vídeo':fontcolor=red:fontsize=90:x=0:y=0"
					// -codec:a copy output.mp4

					// Se añade coma antes de la nota, ya que forma parte de la sentencia FFMPEG.
					if (n > 0)
						resultado = resultado + ",";

					resultado = resultado
							+ "drawtext=fontfile=" + fichero_fuente + ":text='"
							+ texto
							+ "':fontcolor=" + hex + ":fontsize=" + tam + ":x=" + posX + ":y=" + posY + duracion;
				}

			}
		}
		logger.info("--- Compuesta sentencia resultado = {} ", resultado);
		return resultado;
	}

	/**
	 * Método que genera, a partir del total de archivos generados, la cadena de
	 * concatenación adecuada para generar el vídeo final.
	 */
	private String generaCadenaConcat(String directorio) {
		String resultado = "\"concat:";

		for (int i = 0; i < getTotalArchivos(); i++) {
			if (i == 0)
				resultado = resultado.concat(directorio + File.separator + "tmp0.ts");
			else
				resultado = resultado.concat("|" + directorio + File.separator + "tmp" + i + ".ts");
		}

		return resultado + "\"";
	}

	public int getTotalArchivos() {
		return totalArchivos;
	}

	public void setTotalArchivos(int totalArchivos) {
		this.totalArchivos = totalArchivos;
	}

	/**
	 * Método para crear una secuencia de imágenes que FFMPEG podrá convertir en
	 * vídeo.
	 */
	private Vector<List<String>> preparaSecuenciaArchivos(List<TFMPeticion> peticion, String directorioSesion,
			String directorioDestinoVideo) {
		// Definición de variables.
		String nombre, duracion, desde, hasta, precision, texto = "";
		String cadenaNotas = ""; // String con la secuencia FFMPEG para crear notas para una imagen.
		List<String> tmp_comando = null;
		Vector<List<String>> resultado = new Vector<List<String>>();
		List<TFMNota> notas = null;
		Path archivoOrigen;

		logger.warn("preparaSecuenciaArchivos: INICIANDO para peticion " + peticion + " de tamaño " + peticion.size());

		for (int i = 0; i < peticion.size(); i++) {
			// Recuperamos el archivo en la posición i-ésima
			nombre = ((TFMPeticion) peticion.get(i)).getNombre();

			// Referenciamos la ruta del archivo en disco.
			archivoOrigen = Paths.get(directorioSesion + File.separator + nombre);

			// Si es una imagen
			if (TFMUtils.esIMAGEN(archivoOrigen)) {

				duracion = ((TFMPeticion) peticion.get(i)).getDuracion();
				precision = ((TFMPeticion) peticion.get(i)).getPrecision();
				logger.warn("preparaSecuenciaArchivos: procesando comando para {} con duracion {} ", nombre, duracion);

				// El primer comando genera un mp4.
				// La concatenación de mp4 da problemas, por lo que se generará primero el mp4 y
				// luego el ts que se anexará
				/*
				 * tmp_comando = Arrays.asList(rutaFFMPEG, "-y", "-r", "1/2", "-loop", "1",
				 * "-framerate", "30", "-t",
				 * "10", "-i", archivoOrigen.toString(), "-f", "lavfi", "-i", "aevalsrc=0",
				 * "-shortest", "-vf",
				 * "\"scale=1280:720:force_original_aspect_ratio=1,pad=1280:720:(ow-iw)/2:(oh-ih)/2\"",
				 * "-threads",
				 * "4", "-r", "30",
				 * directorioDestinoVideo + File.separator + "tmp" + i + ".mp4");
				 */
				tmp_comando = Arrays.asList(rutaFFMPEG, "-y",
						"-r", "1/2",
						"-loop", "1",
						"-framerate", "30",
						"-t", duracion,
						"-i", archivoOrigen.toString(),
						"-f", "lavfi",
						"-i", "aevalsrc=0",
						"-shortest",
						"-vf", "\"scale=1280:720:force_original_aspect_ratio=1,pad=1280:720:(ow-iw)/2:(oh-ih)/2\"",
						"-tune:v", "stillimage",
						"-preset", "ultrafast",
						"-threads", "8",
						"-r", "30",
						directorioDestinoVideo + File.separator + "tmp" + i + ".mp4");

				/* 				tmp_comando = Arrays.asList(rutaFFMPEG, "-y", "-r", "1/2", "-loop", "1", "-framerate", "30", "-t",
										duracion, "-i", archivoOrigen.toString(), "-f", "lavfi", "-i", "aevalsrc=0", "-shortest", "-vf",
										"\"scale=1280:720:force_original_aspect_ratio=1,pad=1280:720:(ow-iw)/2:(oh-ih)/2\"", "-threads",
										"4", "-r", "30",
										directorioDestinoVideo + File.separator + "tmp" + i + ".mp4"); */

				resultado.add(tmp_comando);

				// El segundo comando genera un .ts que será más seguro al anexar.
				tmp_comando = Arrays.asList(rutaFFMPEG, "-y", "-i",
						directorioDestinoVideo + File.separator + "tmp" + i + ".mp4", "-vcodec", "h264",
						"-preset", "fast",
						"-c:a", "aac",
						"-b:a", "128k",
						// "-r", "30",
						directorioDestinoVideo + File.separator + "tmp" + i + ".ts");

				resultado.add(tmp_comando);

				// Se incrementa el número de archivos.
				setTotalArchivos(getTotalArchivos() + 1);
			} //
			else if (TFMUtils.esVideo(archivoOrigen.toString())) // Si es un vídeo
			{
				// Recuperamos los tiempos de acotación del vídeo
				desde = ((TFMPeticion) peticion.get(i)).getDesde();
				hasta = ((TFMPeticion) peticion.get(i)).getHasta();
				precision = ((TFMPeticion) peticion.get(i)).getPrecision();

				// Transformamos los tiempos a milisegundos.
				int milis_desde = Integer.parseInt(desde) * (1000 / Integer.parseInt(precision));
				int milis_hasta = Integer.parseInt(hasta) * (1000 / Integer.parseInt(precision));

				logger.warn(
						"preparaSecuenciaArchivos: procesando comando para {} con rango desde  {} hasta {} con precisión {} ",
						nombre, desde, hasta, precision);

				// Se incrementa el número de archivos.
				setTotalArchivos(getTotalArchivos() + 1);

				// ffmpeg -y -i 21937515_MOV_1311.mp4 -vcodec h264 -preset fast -c:a aac -b:a
				// 128k -threads 8 out0.ts

				// Se transforma a ts para igualar formatos antes de la concatenación.
				tmp_comando = Arrays.asList(rutaFFMPEG, "-y", "-i", archivoOrigen.toString(),
						"-ss", "" + milis_desde + "ms", "-t", "" + (milis_hasta - milis_desde) + "ms",
						"-vcodec", "h264", "-preset", "fast", "-c:a", "aac", "-b:a", "128k", "-threads", "8",
						directorioDestinoVideo + File.separator + "tmp" + i + ".ts");

				resultado.add(tmp_comando);

			}
		}
		return resultado;
	}

}