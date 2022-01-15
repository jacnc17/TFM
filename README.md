# TFM
Repositorio para almacenar la aplicación del TFM

Arrancar el .jar como java -jar tfm-1.0.jar, especificando las variables de entorno
  > file.dirTrabajo
  
  > file.rutaFFMPEG 
  
  > file.rutaFFPROBRE
  
Por ejemplo:
  > java -jar tfm-1.0.jar --file.dirTrabajo="/home/usuario" --file.rutaFFMPEG="/usr/bin/ffmpeg" --file.rutaFFPROBRE="/usr/bin/ffprobe" 

Requisitos de ejecución:
 - JRE11 (desarrollado con OpenJDK17)
 - FFMPEG (incluye FFPROBE)

El fichero .jar incluye todas las librerías de las que depende el proyecto, así como los CSS y JS de terceros, de manera que no es necesaria una conexión a internet cuando se está ejecutando en local.



El trabajo se entrega bajo licencia <a href="http://creativecommons.org/licenses/by-sa/4.0/?ref=chooser-v1">CC BY-SA 4.0</a>.
