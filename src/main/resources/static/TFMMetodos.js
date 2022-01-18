const hashReintentos = new Map();
var hashPropiedades = new Map(); // Propiedades de los vídeos recortados.
var hashDuracionesOriginales = new Map();
var hashPropiedadesNotas = new Map();  // Propiedades de las notas.

var keep = 0; // Permitirá controlar que se cierre la capa emergente cuando se haga click fuera de la capa intermedia.

var en_curso = 0; // Indica si hay alguna llamada en curso 0=no hay llamadas en curso, 1=sí las hay, no se permitirán varias.

const mimesVideo = ['application/x-troff-msvideo', 'video/avi', 'video/msvideo', 'video/x-msvideo', 'video/mp4', 'video/x-matroska'];
const mimesImagenes = ['image/jpeg', 'image/pjpeg', 'image/gif', 'image/png'];

// Granularidad temporal para vídeos (100 = centésimas, 1000 = milésimas, 1 = segundos);
const step = 100;

// Ajuste de tamaño (a más ratio, más grandes serán los recuadros)
var ratio_visualizacion = 0.45;

// Duración por defecto de los clip de imagen
var duracion_defecto_img = 10;

// Cargamos JQUERY para la gestión de la edición
var script = document.createElement('script');
script.src = '/jquery/jquery-3.6.0.min.js';
script.type = 'text/javascript';
document.getElementsByTagName('head')[0].appendChild(script);

var script2 = document.createElement('script');
script2.src = '/jquery/jquery-ui.js';
script2.type = 'text/javascript';
document.getElementsByTagName('head')[0].appendChild(script2);

// Cargamos el JS de gestión de cookies para los elementos de edición
var script3 = document.createElement('script');
script3.src = '/TFMMetodosVariables.js';
script3.type = 'text/javascript';
document.getElementsByTagName('head')[0].appendChild(script3);

// Cargamos el JS de gestión de cookies para las notas
var script4 = document.createElement('script');
script4.src = '/TFMMetodosNotas.js';
script4.type = 'text/javascript';
document.getElementsByTagName('head')[0].appendChild(script4);

// Variables para la gestión de las notas.
var divNotas;
var tabla_duraciones;
var tabla_fragmentos = [];

// Variable para la actualización de la barra de progreso
var intervalo;

// Función que recupera la dirección base de la URL
function getBase() {
    var direccion = window.location.href;
    var resultado = direccion.substring(0, direccion.lastIndexOf("/")) + "/output/";
    return resultado;
}


// Método que se llamará desde el fetch, y que permitirá la verificación del avance del render
function actualiza_barra_progreso (tiempo)
{
    // Inicialmente limpiamos el posible intervalo previo
    clearInterval(intervalo);
    // console.log("actualiza_barra_progreso ", tiempo);

    // Se recompone el intervalo de consultas
    intervalo = setInterval(function () {

        fetch("/getInfoOperacion/")
            .then(response => response.text())
            .then((response) => {
                // alert(response);
                console.log("actualiza_barra_progreso ", response);

                if (response == '') {
                    document.getElementById("progreso").style.height="0px";
                    document.getElementById("progreso_actual").style.height="0px";
                }
                    
                else
                {
                    if (response == 100)
                    {
                        document.getElementById("progreso").style.height="0px";
                        document.getElementById("progreso_actual").style.height="0px";
                        clearInterval(intervalo);

                    }
                    else 
                    {
                        document.getElementById("progreso").style.height = "10px";
                        document.getElementById("progreso_actual").style.height = "10px";
                        document.getElementById("progreso_actual").style.width = response + "%";
                    }

                }
            })
    }, tiempo);
}

// Funcion que mostrará el mensaje de ayuda en la ventana de recorte de imagen
function muestraAyuda1() {
    // Get the snackbar DIV
    var x = document.getElementById("msjAyuda1");

    // Add the "show" class to DIV
    x.className = "show";

    // After 3 seconds, remove the show class from DIV
    setTimeout(function () { x.className = x.className.replace("show", ""); }, 3000);
}


// Funcion que mostrará el mensaje de ayuda en la ventana de recorte de vídeo
function muestraAyuda2() {
    // Get the snackbar DIV
    var x = document.getElementById("msjAyuda2");

    // Add the "show" class to DIV
    x.className = "show";

    // After 3 seconds, remove the show class from DIV
    setTimeout(function () { x.className = x.className.replace("show", ""); }, 3000);
} 

// Añade la capa oculta con el item que se mostrará cuando se quiera editar un elemento.
function anadir_capa_oculta(nombre_archivo, idSesion, mime) {
    if (mime) {
        // console.log("anadir_capa_oculta: CREANDO CAPA OCULTA PARA '" + nombre_archivo + ",', mime = ", mime);

        var iDiv = document.createElement('div');
        iDiv.id = "capa_oculta_" + nombre_archivo;
        iDiv.className = "capa_edicion_imagen";
        iDiv.onclick = function () { oculta_capa('capa_oculta_' + nombre_archivo);   recalcula_propiedades_cookies()  };

        var divIntermedia = document.createElement('div');
        divIntermedia.onclick = function () { keep = 1; /* Si hacemos click en la capa intermedia, no se cerrará por click en la capa de edición */ };

        // Gestión en función del mime
        if (mimesImagenes.some(i => mime.includes(i))) // Gestión de imágenes en capa oculta (para la edición).
        {
            divIntermedia.className = "capa_intermedia_edicion_imagen";
            divIntermedia.id = "capa_intermedia_" + nombre_archivo;

// Marco de título
var titulo = document.createElement('div');
titulo.style.display="table";
titulo.style.width="100%";
titulo.style.paddingLeft="20px";
titulo.style.paddingRight="20px";

var tituloMarco =  document.createElement('div');
tituloMarco.style.width="80%";
tituloMarco.style.paddingBottom="20px";
tituloMarco.style.overflow="hidden";
tituloMarco.innerHTML = "<strong>Fichero: "+nombre_archivo.substring(nombre_archivo.indexOf("_")+1)+"</strong>"; 
tituloMarco.style.display="table-cell";


var ayuda =  document.createElement('div');
ayuda.style.width="20%";
ayuda.style.height="20px";
ayuda.innerHTML="<a href='#' onclick='muestraAyuda1()'>?</a>";
ayuda.style.textAlign="right";
ayuda.style.display="table-cell";
//ayuda.onclick="alert('holi')";



titulo.append(tituloMarco);
titulo.append(ayuda);

            // Marco de edición de proporciones 16/9
            var divMarcoEdicion = document.createElement('div');
            divMarcoEdicion.className = "clase_marco_oculto";

            var origen_elemento_mini = getBase() + idSesion + '/' + nombre_archivo;
            var imgElement = document.createElement('img');
            imgElement.id = 'img_oculta_' + nombre_archivo;
            imgElement.className = "clase_imagen_oculta";

            // Función onError
            imgElement.onerror = function ()  // En caso de error en la carga, se intenta recargar.
            {
                setTimeout(function () { anadir_img_oculta(origen_elemento_mini, "img_oculta_" + nombre_archivo, mime); }, 1000);
            }

            // Se asignan los elementos generados.
            imgElement.src = origen_elemento_mini;

divMarcoEdicion.appendChild(titulo);   

            divMarcoEdicion.appendChild(imgElement);




            divIntermedia.appendChild(divMarcoEdicion);

            iDiv.appendChild(divIntermedia);


        }
        else if (mimesVideo.some(i => mime.includes(i)))  // Gestión de vídeo en capa oculta. Debe esperarse a que FFMPEG lo trnansforme.
        {
            // Se asignan los atributos para la capa contenedora
            divIntermedia.className = "capa_intermedia_edicion_imagen";
            divIntermedia.id = "capa_intermedia_" + nombre_archivo + ".mp4";
divIntermedia.style.backgroundColor = "rgb(218, 129, 136)"; // #da8188 rgb(218, 129, 136)

// Marco de título
var titulo = document.createElement('div');
titulo.style.display="table";
titulo.style.width="100%";
/* titulo.style.paddingLeft="20px";
titulo.style.paddingRight="20px"; */

var tituloMarco =  document.createElement('div');
tituloMarco.style.width="80%";
tituloMarco.style.paddingBottom="10px";
tituloMarco.style.overflow="hidden";
tituloMarco.innerHTML = "<strong>Fichero: "+nombre_archivo.substring(nombre_archivo.indexOf("_")+1)+"</strong>"; 
tituloMarco.style.display="table-cell";


var ayuda =  document.createElement('div');
ayuda.style.width="20%";
ayuda.style.height="20px";
ayuda.innerHTML="<a href='#' style='color:black' onclick='muestraAyuda2()'>?</a>";
ayuda.style.textAlign="right";
ayuda.style.display="table-cell";
//ayuda.onclick="alert('holi')";



titulo.append(tituloMarco);
titulo.append(ayuda);


            // Marco de edición de proporciones 16/9
            var divMarcoEdicion = document.createElement('div');
            divMarcoEdicion.className = "clase_marco_oculto";

divMarcoEdicion.style.maxHeight="400px";
divMarcoEdicion.style.height="400px";

            // Creamos un elemento de vídeo mini (reducido en resolución) y se le asignan atributos.
            var origen_elemento_mini = getBase() + idSesion + '/miniaturas/mini_' + nombre_archivo + ".mp4";
            var videoElement = document.createElement('video');
            // videoElement.controls = "controls";
            videoElement.className = "clase_video_oculto";
            videoElement.id = 'video_oculto_' + nombre_archivo;

            // Función onError
            videoElement.onerror = function ()  // En caso de error en la carga, se intenta recargar.
            {
                setTimeout(function () { anadir_video_oculto(origen_elemento_mini, "video_oculto_" + nombre_archivo, mime); }, 1000);
            }

            // Se asignan los elementos generados.
            videoElement.src = origen_elemento_mini;
            console.log("Video. Se asignan los elementos generados.");


divMarcoEdicion.appendChild(titulo);   


            divMarcoEdicion.appendChild(videoElement);
            divIntermedia.appendChild(divMarcoEdicion);

            iDiv.appendChild(divIntermedia);

        }





        document.getElementById('principal').appendChild(iDiv);
    }
    else {
        // OK console.log("anadir_capa_oculta: NO HAY MIME TYPE RECONOCIDO PARA '" + nombre_archivo + ",', SE IGNORA'");
    }
}


// Añade la capa oculta con la imagen que se mostrará cuando se quiera editar un elemento.
function anadir_img_oculta(nombre_archivo, id, mime) {
    console.log("anadir_img_oculta: entrando para capa '" + id + "'");

    var id_img_mini = document.getElementById(id);

    // Gestión de reintentos
    if (valor = hashReintentos.get(id)) {
        hashReintentos.set(id, valor + 1);
        console.log("anadir_img_oculta: reintento ", id, ",: ", valor);
        // TODO: imponer un máximo de reintentos.
    }
    else {
        console.log("anadir_img_oculta: primer reintento");
        hashReintentos.set(id, 1);
    }
    console.log("anadir_img_oculta: actualizando src");

    id_img_mini.src = nombre_archivo;
    id_img_mini.src.onError = "setTimeout(() => {  anadir_img_oculta(\"" + nombre_archivo + "\", \"" + id + "\", \"" + mime + "\")}, 500);"
}


// Añade la capa oculta con el item que se mostrará cuando se quiera editar un elemento.
function anadir_video_oculto(nombre_archivo, id, mime) {

    console.log("anadir_video_oculto: entrando para capa '" + id + "'");

    var id_vid_mini = document.getElementById(id);

    // alert(id_vid_mini);
    // Gestión de reintentos
    if (valor = hashReintentos.get(id)) {
        hashReintentos.set(id, valor + 1);
        console.log("anadir_video_oculto: reintento ", id, ",: ", valor);
        // TODO: imponer un máximo de reintentos.
    }
    else {
        console.log("anadir_video_oculto: primer reintento");
        hashReintentos.set(id, 1);
    }
    console.log("anadir_video_oculto: actualizando src");

    id_vid_mini.src = nombre_archivo;
    id_vid_mini.src.onError = "setTimeout(() => {/* alert('retry'); */ anadir_video_oculto(\"" + nombre_archivo + "\", \"" + id + "\", \"" + mime + "\")}, 500);"
}


// Función que elimina todos los elementos que se lleven al div "papelera".
function borra() {
    var papelera = document.getElementById("papelera");
    // ('borrando!');

    var divs = papelera.getElementsByTagName('div');

    // A priori sólo habrá uno, pero se hace un bucle.
    for (var i = 0; i < divs.length; i += 1) {
        // Se elimina de la tabla de propiedades.
        // console.log ("Borrando ",divs[i].id);
        hashPropiedades.delete(divs[i].id);

        papelera.removeChild(divs[i]);
    }

    // Se actualiza el valor de las cookies
    recalcula_propiedades_cookies ()
}

// Función para determinar en segundos la duración de un vídeo
function get_duracion(itemID) {
    // Recuperamos el elemento vídeo
    var video = document.getElementById("video_oculto_" + itemID); // Se añade prefijo del vídeo intermedio mp4 (convertido en servidor).
    var seconds;
    console.log("get_duracion : recuperando duración de  video_oculto_" + itemID + ", video = " + video + ", duracion =  " + video.duration + ",estado =" + video.readyState);

    seconds = video.duration;

    return seconds * step;
}


// Método que recalcula la visibilidad de las notas en el área de edición.
function notas_actualiza_visibilidad(valor_slide) {
    // Recorremos hashPropiedadesNotas para establecer la visibilidad.
    for (i = 0; i < hashPropiedadesNotas.size; i++) {
        let id_posicion = Array.from(hashPropiedadesNotas.keys())[i];
        // console.log("SLIDE: chequeando ", id_posicion, " con valor ", hashPropiedadesNotas.get(id_posicion));

        let rango = id_posicion.substr(0, 3);

        // Comprobamos si encontramos un tag de inicio.
        if (rango == "ini") {
            var id_nota = id_posicion.substr(4);

            var valor_ini = hashPropiedadesNotas.get(id_posicion);
            var valor_fin = hashPropiedadesNotas.get("fin_" + id_nota);

            // console.log("SLIDE: chequeando rango ", rango, " en ", valor_slide, " con id_nota ", id_nota, ", valor ini =", valor_ini, ", valor_fin  =", valor_fin);

            if (eval(valor_ini) <= eval(valor_slide) && eval(valor_fin) >= eval(valor_slide)) {
                document.getElementById(id_nota).style.visibility = "inherit";
            }
            else {
                document.getElementById(id_nota).style.visibility = "hidden";
            }
        }
    }
}

function notasH1(elemento) {
    console.log ("notasH1: elemento =",elemento);
    let recuadro = document.getElementById(elemento).getElementsByClassName("clase_recuadro_texto");
    /*     console.log ("document.getElementById(elemento).style.top  = ",document.getElementById(elemento).style.top)
        document.getElementById(elemento).style.top = (document.getElementById(elemento).style.top - 20)+"px"; */



    recuadro[0].style.fontSize = "6em";
}

function notasH2(elemento) {
    let recuadro = document.getElementById(elemento).getElementsByClassName("clase_recuadro_texto");
    recuadro[0].style.fontSize = "3em";
}

function notasH3(elemento) {
    let recuadro = document.getElementById(elemento).getElementsByClassName("clase_recuadro_texto");
    recuadro[0].style.fontSize = "1em";
}

function notasSetFuente(padre, fuente, nombre) {
    // console.log("notasSetFuente. padre=",padre);
    let cuadroTexto = document.getElementById(padre).getElementsByClassName("clase_recuadro_texto")[0];

    estiloAnterior = cuadroTexto.className;
    claseFinal = "clase_recuadro_texto " + fuente;

    if (estiloAnterior.includes("bold"))
        claseFinal = claseFinal + " bold";
    if (estiloAnterior.includes("italic"))
        claseFinal = claseFinal + " italic";

    cuadroTexto.className = claseFinal;

}

function notasToggleNegrita(elemento) {
    let recuadro = document.getElementById(elemento).getElementsByClassName("clase_recuadro_texto")[0];

    if (recuadro != null && recuadro.className.includes("bold"))
        recuadro.className = recuadro.className.replace(" bold", "");
    else if (recuadro != null)
        recuadro.className = recuadro.className + " bold";


}

function notasToggleCursiva(elemento) {
    let recuadro = document.getElementById(elemento).getElementsByClassName("clase_recuadro_texto")[0];

    if (recuadro != null && recuadro.className.includes("italic"))
        recuadro.className = recuadro.className.replace(" italic", "");
    else if (recuadro != null)
        recuadro.className = recuadro.className + " italic";
}




// Muestra la ventana emergente de edición
function ventana_emergente(itemID) {
    // alert ("hola "+itemID+", del original "+nombre_original);

    // Se recupera la información de la capa cuyo detalle se va a mostrar.
    let nombre_original = document.getElementById(itemID).getAttribute('data-nombre');
    let mimetypeArchivo = document.getElementById(itemID).getAttribute('mime-type');

    // alert(mimetypeArchivo);

    let capa = document.getElementById('capa_oculta_' + nombre_original);
    let capa_edicion = capa.childNodes[0]; // La capa oculta tendrá sólo un hijo
    let elemento = document.createElement("div");
    elemento.id = "layer_info_activa";

    // console.log ("Ventana emergente : " + capa.id + ", id= "+itemID+", del original "+nombre_original);

    // Nos aseguramos de que no hay más información en la capa.
    if (document.getElementById("layer_info_activa"))
        document.getElementById("layer_info_activa").remove();

    //////////// Gestión de notas
    oculta_todas_notas();

    // En función del mime, se asignan los estilos.
    if (mimesVideo.some(v => mimetypeArchivo.includes(v)))  // Si queremos editar un vídeo
    {
        let desde = hashPropiedades.get(itemID).inicio;
        let hasta = hashPropiedades.get(itemID).fin;
        let duracion = hashPropiedades.get(itemID).duracion_video;

        // console.log("ventana_emergente: itemID = " + itemID + ", inicio = " + desde + ", hasta =" + hasta + ", duracion = ",duracion);

        elemento.appendChild(genera_etiquetas_slide_rango());
        elemento.appendChild(genera_slide_rango(itemID, desde, hasta, duracion, hashPropiedades.get(itemID).nombre_original));
        capa_edicion.appendChild(elemento); // Después de la imagen o el vídeo pondremos el área de detalle.

    }
    else if (mimesImagenes.some(i => mimetypeArchivo.includes(i)))  // Si queremos editar una imagen
    {

        // Recuperamos el valor actual de la duración (segundos para una imagen) o el rango para un vídeo.
        // elemento.innerHTML = itemID + ": " + hashPropiedades.get(itemID).duracion + " segundos";
        elemento.appendChild(genera_etiquetas_slide());
        elemento.appendChild(genera_slide(itemID, hashPropiedades.get(itemID).duracion, hashPropiedades.get(itemID).nombre_original));

        capa_edicion.appendChild(elemento); // Después de la imagen o el vídeo pondremos el área de detalle.
    }


    capa.style.visibility = "visible";
    capa.style.zIndex = 100;
}




// Método que genera un selector de rango para delimitar el inicio y el fin de un clip de vídeo.
// JQUERY https://jqueryui.com/slider/#range


function genera_slide_rango(itemID, desde, hasta, duracion, nombre_orig) {
    let mi_slider = document.createElement("div");
    mi_slider.id = "slider-range";
    mi_slider.style.width = "95%";
    mi_slider.style.margin = "auto";

    var video = document.getElementById("video_oculto_" + nombre_orig); // Se añade prefijo del vídeo intermedio mp4 (convertido en servidor).
    video.currentTime = desde / step;

    $(function () {
        $("#slider-range").slider({
            id: "mi_slider",
            range: true,
            min: 0,
            max: duracion, // Hay que sacar la duración del vídeo
            values: [desde, hasta],
            create: function (event, ui) {
                $("#amount").val("Recorte desde el segundo " + (desde / step) + " hasta el " + (hasta / step).toFixed(2));
            },
            slide: function (event, ui) {
                // console.log(ui);

                let desde_anterior = hashPropiedades.get(itemID).inicio;
                // let hasta_anterior = hashPropiedades.get(itemID).fin;

                if (ui.values[0] != desde_anterior) // Si ha cambiado el primer rango
                    video.currentTime = ui.values[0] / step;
                else // Se cambió el segundo rango.
                    video.currentTime = ui.values[1] / step;

                hashPropiedades.set(itemID, { nombre_original: nombre_orig, duracion_video: duracion, inicio: ui.values[0], fin: ui.values[1] });

                let desde = (ui.values[0] / step);
                let hasta = (ui.values[1] / step);
                $("#amount").val("Recorte desde el segundo " + desde + " hasta el " + hasta);

                // Se recalcula el nuevo ancho
                // console.log("genera_slide_rango = recalculando ancho. Nueva duracion = " + hasta + " - " + desde);
                // console.log("genera_slide_rango = recalculando ancho. Nueva duracion = " + eval(hasta - desde));
                document.getElementById(itemID).style.minWidth = eval(hasta - desde) * 10 * ratio_visualizacion + "px";
                document.getElementById(itemID).style.maxWidth = eval(hasta - desde) * 10 * ratio_visualizacion + "px";

                // Gestión de info en cookies referente a la información de rangos, incio y fin, duración, etc.
                recalcula_propiedades_cookies();
            }
        });
    });

    return mi_slider;
}


// Método que genera las etiquetas asociadas al slider.
// JQUERY https://jqueryui.com/slider/#rangemin
function genera_etiquetas_slide_rango() {
    let parrafo = document.createElement("p");
    let etiq = document.createElement("label");
    let input_texto = document.createElement("input");

    etiq.for = "amount";

    input_texto.type = "text";
    input_texto.id = "amount"; // Coincidirá también con el de genera_slide
    input_texto.style = "padding: 0px 10px 10px; width:100%; border:0 ; font-color:rgba(0,0,0,.55);; background-color: transparent;outline: none;";
    input_texto.readOnly = true;
    input_texto.style.outline = "none";

    parrafo.appendChild(etiq);
    parrafo.appendChild(input_texto);

    return parrafo;
}


// Método que genera un selector para determinar la duración de un clip de imagen
// JQUERY https://jqueryui.com/slider/#rangemin
function genera_slide(itemID, tiempo, nombre_orig) {
    let mi_slider = document.createElement("div");
    mi_slider.id = "slider-range-min";
    mi_slider.style.width = "95%";
    mi_slider.style.margin = "25px auto auto";
    // mi_slider.style.marginTop = "10px";

    $(function () {
        $("#slider-range-min").slider({
            id: "mi_slider",
            range: "min",
            value: tiempo,
            min: 1,
            max: 60,
            slide: function (event, ui) {
                $("#amount").val("Duración: " + ui.value + " segundos.");

                hashPropiedades.set(itemID, { nombre_original: nombre_orig, duracion: ui.value });
                // console.log("genera_slide : " + nombre_orig + ", duracion " + hashPropiedades.get(itemID).duracion);
                document.getElementById(itemID).style.minWidth = ui.value * 10 * ratio_visualizacion + "px";
                document.getElementById(itemID).style.maxWidth = ui.value * 10 * ratio_visualizacion + "px";

                // Gestión de info en cookies referente a la información de rangos, incio y fin, duración, etc.
                recalcula_propiedades_cookies();
            }
        });
        $("#amount").val("Duración: " + $("#slider-range-min").slider("value") + " segundos.");

    });

    return mi_slider;
}

// Método que genera las etiquetas asociadas al slider.
// JQUERY https://jqueryui.com/slider/#rangemin
function genera_etiquetas_slide() {
    let parrafo = document.createElement("p");
    let etiq = document.createElement("label");
    let input_texto = document.createElement("input");

    etiq.for = "amount";
    input_texto.type = "text";
    input_texto.id = "amount"; // Coincidirá también con el de genera_slide
    input_texto.style = "padding:20px 10px 10px 10px; width:100%; border:0; font-color:rgba(0,0,0,.55);; background-color: transparent;"
    input_texto.readOnly = true;
    input_texto.style.outline = "none";

    parrafo.appendChild(etiq);
    parrafo.appendChild(input_texto);

    return parrafo;

}





// Método que genera un selector para determinar la duración de un clip de imagen
// JQUERY https://jqueryui.com/slider/#rangemin
function genera_slide_notas(duracion) {
    // Se borra el anterior
    anterior = document.getElementById("mi_slider_notas");
    if (anterior != null) {
        // console.log("Se quita el anterior");
        anterior.remove();
    }

    let mi_slider_notas = document.createElement("div");
    mi_slider_notas.id = "slider_notas";
    mi_slider_notas.style.width = "95%";
    mi_slider_notas.style.margin = "auto";

    /*  width: 80%;
     align-self: center;
     margin: auto; */

    $(function () {
        $("#slider_notas").slider({
            id: "mi_slider_notas_slider",
            range: "min",
            value: 0,
            min: 0,
            max: duracion,
            create: function (event, ui) {
                // console.log(tabla_duraciones);
                muestra_captura(0, 0, tabla_fragmentos[0]);

                // Actualizamos visibilidad de notas
                notas_actualiza_visibilidad(0);

                // Almacenamos en la variable "pos" el valor inicial 0.
                $("#slider_notas").attr("pos", "0");
            },
            slide: function (event, ui) {
                $("#posicion_nota").val("Posición: " + ui.value / step + " segundos.");

                // Actualizamos visibilidad de notas
                notas_actualiza_visibilidad(ui.value);

            },
            stop: function (event, ui) {
                $("#posicion_nota").val("Posición: " + ui.value / step + " segundos.");

                // Almacenamos en la variable "pos" la ubicación del slider.
                $("#slider_notas").attr("pos", ui.value);

                // Gestión de visibilidad de notas
                let valor_slide = document.getElementById("slider_notas").getAttribute("pos");

                // Actualizamos visibilidad de notas
                notas_actualiza_visibilidad(valor_slide);



                const iterator1 = tabla_duraciones.keys();
                var posicion = 0;
                var inicio = tabla_duraciones.keys().next();
                var actual;
                // console.log (tabla_duraciones.size);
                // console.log ("1posicion  = ",posicion);

                // Determinamos el clip al que pertenece el momento del slider.
                for (i = 0; i < tabla_duraciones.size; i++) {
                    // console.log ("2posicion  = ",posicion, " i = ",i);

                    actual = iterator1.next();
                    // console.log (ui.value, ", ", actual.value);
                    if (eval(ui.value) > eval(actual.value)) {
                        // console.log ("Encontrado en ",ui.value, " con i = ",i);
                        posicion = i;
                        inicio = actual;
                        // console.log ("3osicion  = ",posicion);
                    }

                }
                // console.log("inicio = ",inicio);
                // console.log('El clip es el que está en posición ', posicion, " con inicio en ", inicio.value);
                var offset = ui.value - inicio.value;
                // console.log('offset = ', offset);
                // console.log('imagen = ', tabla_duraciones.get(inicio.value));
                // console.log('document.getElementById("imagen_oculta_"+tabla_duraciones.get(inicio.value))  = ', document.getElementById("img_oculta_" + tabla_duraciones.get(inicio.value)));

                muestra_captura(inicio.value, posicion, offset);

            }
        });
        // console.log("EN principal");

        $("#posicion_nota").val("Posición: 0.00 segundos.");

    });

    return mi_slider_notas;
}



// Método que genera las etiquetas asociadas al slider.
// JQUERY https://jqueryui.com/slider/#rangemin
function genera_etiquetas_slide_notas() {
    let parrafo = document.createElement("p");
    let etiq = document.createElement("label");
    let input_texto = document.createElement("input");

    etiq.for = "posicion_nota";
    parrafo.style =  "padding-top: 10px !important; padding-bottom: 35px !important;";

    // etiq.textContent = "Posición: ";

    input_texto.type = "text";
    input_texto.id = "posicion_nota"; // Coincidirá también con el de genera_slide
    input_texto.readOnly = true;
    
    //  input_texto.style = "border:0; color:#f6931f; font-weight:bold;"
    input_texto.style = "padding:10px; width:100%; border:0; font-color:rgba(0,0,0,.55);; background-color: transparent; outline: none;"

    parrafo.appendChild(etiq);
    parrafo.appendChild(input_texto);

    return parrafo;
}


// Recupera la captura en un tiempo determinado de la composición final.
function muestra_captura(tiempo, posicion, offset) {
    imagen = (document.getElementById("img_oculta_" + tabla_duraciones.get(tiempo)));
    marco = document.getElementById("marco_oculto_notas");

    if (imagen != null) {
        if (document.getElementById("img_temporal") != null)
            document.getElementById("img_temporal").src = imagen.src;

        else {
            nueva_imagen = document.createElement('img');

            nueva_imagen.src = imagen.src;
            // nueva_imagen.className = "clase_marco_oculto_notas";
            nueva_imagen.className = "clase_imagen_oculta";
            nueva_imagen.id = "img_temporal";

            marco.appendChild(nueva_imagen);
        }
    }
    else {
        video = document.getElementById("video_oculto_" + tabla_duraciones.get(tiempo))
        var inicio = tabla_fragmentos[posicion]; // inicio del vídeo
        // console.log("El vídeo empieza en ", inicio);

        // Se calcula el fotograma a mostrar
        var pos_real = inicio + offset;
        video.currentTime = eval(pos_real / step);

        // Se calcula cuando haya acabado la búsqueda, si no podría capturar la imagen del fotograma antes mostrado
        video.onseeked = (event) => {

            // console.log("Debe mostrarse el fotograma en ", pos_real, ", video.currentTime = ", video.currentTime);
            // Repinta el fotograma
            const canvas = document.createElement("canvas");
            canvas.id = "canvas_captura";
            canvas.width = "832";
            canvas.height = "468";
            canvas.getContext('2d').drawImage(video, 0, 0, canvas.width, canvas.height);
            // console.log(canvas);
            // console.log(canvas.toDataURL());

            if (document.getElementById("img_temporal") != null) {
                // console.log("caso 1");
                document.getElementById("img_temporal").src = canvas.toDataURL();
            }
            else {
                // console.log("caso 2");

                nueva_imagen = document.createElement('img');

                nueva_imagen.src = canvas.toDataURL();
                nueva_imagen.className = "clase_imagen_oculta";
                nueva_imagen.id = "img_temporal";

                marco.appendChild(nueva_imagen);
            }

        };


    }
}


// Función que abrirá la sección de edición de notas
function muestra_capa_notas() {
    // Recuperamos la duración actual del vídeo (sólo los elementos en la zona de edición).
    let elementosEdicion = document.getElementById("miZonaEdicion").childNodes;

    // Variables para la gestión de duraciones
    let duracion_total = 0;
    let inicio = 0;
    tabla_duraciones = new Map();
    tabla_fragmentos = [];

    if (elementosEdicion == undefined || elementosEdicion.length == 0) {
        alert ("¡No hay clips en la zona de edición!");
    }
    else {
        // Recorremos el área de edición para mostrar los items EN ORDEN
        // Originalmente se excluía el primer elemento, que era un texto en blanco para forzar el tamaño del área
        for (var ele = 0; ele < elementosEdicion.length; ele++) {
            let nombre = hashPropiedades.get(elementosEdicion[ele].id).nombre_original;
            let duracion = hashPropiedades.get(elementosEdicion[ele].id).duracion;
            let desde = Math.round(hashPropiedades.get(elementosEdicion[ele].id).inicio); // En caso de que llegue con decimales.
            let hasta = Math.round(hashPropiedades.get(elementosEdicion[ele].id).fin); // En caso de que llegue con decimales.

            if (!duracion)
                duracion = 0;

            if (!desde)
                desde = 0;

            if (!hasta)
                hasta = 0;

            console.log('duracion = ', duracion, ', desde = ', desde, ', hasta = ', hasta);

            // Recogemos la duración total.
            duracion_total = eval(duracion_total + duracion * step + (hasta - desde));
            // OK para vídeo alert(hasta);

            // Almacenamos rangos 
            tabla_duraciones.set(inicio, nombre);

            // Almacenamos los puntos de inicio del vídeo original (se guardan también para imagen aunque no se usen).
            tabla_fragmentos.push(desde);

            inicio = duracion_total;

        }
        var div_notas = get_div_notas(duracion_total);
        div_notas.style.visibility = "visible";

        console.log("Duración total = ", duracion_total / step);

    }

}


// Método que devuelve el objeto divNotas, recuperado o creado si no existe.
function get_div_notas(duracion) {
    if (divNotas == undefined) {
        console.log("Hay que crear nueva capa");

        divNotas = document.createElement('div');
        divNotas.id = "capa_notas";
        divNotas.className = "capa_edicion_imagen";

        divNotas.onclick = function () { oculta_capa('capa_notas'); };

        var divIntermedia = document.createElement('div');
        divIntermedia.onclick = function () { keep = 1; };
        divIntermedia.className = "capa_intermedia_edicion_nota";
        divIntermedia.id = "capa_intermedia_edicion_notas";

        // Marco de edición de proporciones 16/9
        var divMarcoEdicion = document.createElement('div');
        divMarcoEdicion.className = "clase_marco_oculto_notas";
        divMarcoEdicion.id = "marco_oculto_notas";
        divMarcoEdicion.onclick = function () { add_nota(event); };
        /*         divMarcoEdicion.style.position ="fixed";
                divMarcoEdicion.style.top ="0px";
                divMarcoEdicion.style.left ="0px"; */

        // Recuperamos el valor actual de la duración (segundos para una imagen) o el rango para un vídeo.
        // elemento.innerHTML = itemID + ": " + hashPropiedades.get(itemID).duracion + " segundos";
        divIntermedia.appendChild(divMarcoEdicion);
        divIntermedia.appendChild(genera_etiquetas_slide_notas());
        
        // Consideramos la posibilidad de que no haya aún duración calculada (puede pasar si se recarga 
        // la info de las notas desde cookies)
        if (duracion > -1)
            divIntermedia.appendChild(genera_slide_notas(duracion));

        divNotas.appendChild(divIntermedia);

        document.getElementById("principal").appendChild(divNotas);

    }
    else // Hay que actualizar la duración
    {
        if (document.getElementById("slider_notas") != null) {
            let slider_old = document.getElementById("slider_notas").remove();
        }

        var divIntermedia = document.getElementById("capa_intermedia_edicion_notas");
        divIntermedia.appendChild(genera_slide_notas(duracion));
    }

    divNotas.style.zIndex = "1000";

    return divNotas;
}

function saluda() { console.log('hola'); }




// Método que añadirá una nota
function add_nota(evento) {
    // Se calcula dónde se ha hecho click. Si no hay nota en ese punto, se creará una nueva. Si no, se editará.
    var rect = document.getElementById("marco_oculto_notas").getBoundingClientRect();
    elemento = document.elementFromPoint(evento.clientX, evento.clientY).id;
    obj = document.elementFromPoint(evento.clientX, evento.clientY);
    // console.log("add_nota. click en ", elemento, "con keep = ", keep, " en el objeto = ",obj);

    // Controlamos que no se haga click encima de otra nota (entonces querríamos editarla!)
    // if (keep == 0 && (elemento == "marco_oculto_notas" || elemento == "img_temporal")) {
    if (keep == 0 && (elemento == "img_temporal") ) {
        // console.log("clonando");

        timeStampInMs = window.performance && window.performance.now && window.performance.timing && window.performance.timing.navigationStart ? window.performance.now() + window.performance.timing.navigationStart : Date.now();

        /////////////////// NOTAS 
        var nota = document.getElementById('plantillaNota').cloneNode(true); // true means clone all childNodes and all event handlers;
        nota.id = "NOTA_" + timeStampInMs;
        // nota.className = "nota_oculta";
        nota.style.display = "inherit";
        nota.style.position = "absolute";
        nota.querySelector(".colorpick").id = nota.id + "_color";
        nota.getElementsByClassName("dropdown-menu")[0].setAttribute("nota", nota.id);
        nota.getElementsByClassName("barraMenu")[0].id = "barra_" + nota.id;

        // Creamos el recuadro con el texto de la nota.
        let recuadro_texto = nota.getElementsByClassName("clase_recuadro_texto")[0];
        recuadro_texto.id = "recuadro" + timeStampInMs;
        recuadro_texto.setAttribute("nota_asociada", nota.id);

        let barraMenu = nota.getElementsByClassName("barraMenu")[0];

        // Se ocultan todas las barras de menú para evitar confusión
        let todas_notas = document.getElementsByClassName ("barraMenu");
        for (i = 0; i<todas_notas.length; i++)
        {
            tmp_nota = todas_notas[i];
            // console.log ('ocultando nota ',tmp_nota);
            $(tmp_nota).removeClass("nota_activa"); $(tmp_nota).addClass("nota_oculta"); $(tmp_nota).css("zIndex", "0"); 
        }



        // Condiciones para mostrar los controles del texto. // ORIGINAL : $ nota).hover...
        $(barraMenu).hover(function () { 
            // Se ocultan todas las barras de menú para evitar confusión
            let todas_notas = document.getElementsByClassName ("barraMenu");
            for (i = 0; i<todas_notas.length; i++)
            {
                tmp_nota = todas_notas[i];
                // console.log ('ocultando nota ',tmp_nota);
                $(tmp_nota).removeClass("nota_activa"); $(tmp_nota).addClass("nota_oculta"); $(tmp_nota).css("zIndex", "0"); 
            }

            $(this).removeClass("nota_oculta"); $(this).addClass("nota_activa"); $(this).css("zIndex", "100"); 
        }, 
        function () { 
            $(this).removeClass("nota_activa"); $(this).addClass("nota_oculta"); $(this).css("zIndex", "0");
        });


        // Se añade la nota y se le da estilo.
        document.getElementById("marco_oculto_notas").appendChild(nota);
        nota.style.left = evento.clientX - rect.left - 15 + 'px'; // Se ajusta para que el click coincida con el principio del recuadro de edición

        // Se establece el punto de inicio
        nota_set_inicio(nota.id); // Empieza en la posición del slide
        nota_set_fin_hasta_final(nota.id);  // Por defecto acaba en el fin del video.

        // Evitamos desbordamiento inferior.
        let posY = evento.clientY - rect.top - 25; // Se añade ajuste para que el cuadro de edición coincida con el punto de click, en lugar de que coincida el menú.
        // console.log("posY = ", posY);

        if (posY > 405) 
            posY = 405;

        // console.log("posY = ", posY);
        nota.style.top = posY + 'px';

        // Se hace foco en el recuadro de texto
        limita_ancho(recuadro_texto);
        recuadro_texto.focus();

        // Se recogen los cambios en el color picker
        nota.addEventListener("change", addNota_watch, false);
        nota.querySelector(".colorpick").setAttribute("input_asociado", "recuadro" + timeStampInMs);



        // Añade la capacidad de arrastre
        $(nota).draggable({
            drag: function (event, ui) {
                limita_ancho(recuadro_texto);
                var pos_y = ui.position.top;
                if (pos_y < -30) // Ajuste considerando alto de la barra de menús.
                {
                    ui.position.top = -30;
                }


                let id_recuadro_texto = "recuadro"+$(nota)[0].id.substring(5); // El id de nota viene con prefijo NOTA_
                let id_recuadro_imagen = $(nota)[0].id+"_imagen_nota";

                // Determinamos si es un recuadro de texto
                if (document.getElementById(id_recuadro_texto) != null)
                {

                    // Recuperamos el id del cuadro de texto
                    let tam = document.getElementById(id_recuadro_texto).style.fontSize;
                    let color = document.getElementById(id_recuadro_texto).style.color;
                    let clase = document.getElementById(id_recuadro_texto).className;
                    let desde = hashPropiedadesNotas.get("ini_" + $(nota)[0].id);
                    let hasta = hashPropiedadesNotas.get("fin_" + $(nota)[0].id);


                    let texto =  document.getElementById(id_recuadro_texto).value;
                    add_cookie_nota ( $(nota)[0].id, ui.position.left+"px", ui.position.top+"px", tam, texto, color, clase, desde, hasta);
                }
                else if (document.getElementById(id_recuadro_imagen) != null) // Es una imagen
                {
                    // function add_cookie_imagen(id_nota, izquierda, arriba, tam, desde, hasta, imagen){
                    let tam = document.getElementById(id_recuadro_imagen).style.maxHeight;
                    let desde = hashPropiedadesNotas.get("ini_" + $(nota)[0].id);
                    let hasta = hashPropiedadesNotas.get("fin_" + $(nota)[0].id);
                    let src =  document.getElementById(id_recuadro_imagen).src;
                    let tam_original = document.getElementById(id_recuadro_imagen).getAttribute("tam_original");


                    console.log ("add_nota. Actualizando cookie: ", $(nota)[0].id, "  en rango (",desde,":",hasta,", src = ",src, ",tam = ", tam, ", tam_original =", tam_original);

                    add_cookie_imagen ( $(nota)[0].id, ui.position.left+"px", ui.position.top+"px", tam,  desde, hasta, src, tam_original);
                }


            }
        });


        // Se registra la cookie con la nota (inicialmente sin más propiedades)
        add_cookie_nota (nota.id, nota.style.left, nota.style.top , "1em", "", "", "clase_recuadro_texto", 0, Number.MAX_VALUE );


    }
    else {
        keep = 0;
        // console.log("editando nota ", obj);

        let nombre_barraMenu = "barra_"+obj.getAttribute("nota_asociada");
        let barraMenu = document.getElementById(nombre_barraMenu);
        // console.log("recuperando visibilidad menú  ", barraMenu);

        // Si se recuperó un elemento válido
        if (barraMenu != null)
        {
            // Se ocultan todas las barras de menú para evitar confusión
            let todas_notas = document.getElementsByClassName ("barraMenu");
            for (i = 0; i<todas_notas.length; i++)
            {
                tmp_nota = todas_notas[i];
                // console.log ('ocultando nota ',tmp_nota);
                $(tmp_nota).removeClass("nota_activa"); $(tmp_nota).addClass("nota_oculta"); $(tmp_nota).css("zIndex", "0"); 
            }

            // Se muestra sólo la nota asociada.
            $(barraMenu).removeClass("nota_oculta"); $(barraMenu).addClass("nota_activa"); $(barraMenu).css("zIndex", "100"); 
        }

    }
}



// Método que vigila los cambios de color de un texto.
function addNota_watch(event) {
    // console.log(event);
    // console.log(event.target);
    nombre_nota = event.target;

    let input_asociado = event.target.getAttribute("input_asociado");

    if (input_asociado != null)
        document.getElementById(input_asociado).style.color = event.target.value;

}

// Método que eliminará una nota totalmente de la línea temporal
function nota_borra(id_nota) {
    // Se utiliza la variable keep para evitar el efecto "doble click", que originaba que al borrar una nota se creara otra en el mismo sitio.
    keep = 1;
    document.getElementById(id_nota).remove();


    hashPropiedadesNotas.delete("ini_" + id_nota);
    hashPropiedadesNotas.delete("fin_" + id_nota);

    // console.log(hashPropiedadesNotas);
}

// Expande la nota a lo largo de todo el vídeo
function nota_expande(id_nota) {
    hashPropiedadesNotas.set("ini_" + id_nota, 0);
    nota_set_fin_hasta_final(id_nota);
}


// Determina el instante de inicio de una nota en función de la posición del scroll de tiempo.
function nota_set_inicio(id_nota) {
    hashPropiedadesNotas.set("ini_" + id_nota, document.getElementById("slider_notas").getAttribute("pos"));
    // console.log(hashPropiedadesNotas);
}

// Establece el final de la nota a la posición del slider.
function nota_set_fin(id_nota) {
    hashPropiedadesNotas.set("fin_" + id_nota, document.getElementById("slider_notas").getAttribute("pos"));
    // console.log(hashPropiedadesNotas);
}

// Establece el final de la nota al fin del vídeo.
function nota_set_fin_hasta_final(id_nota) {
    hashPropiedadesNotas.set("fin_" + id_nota, Number.MAX_VALUE);
    // console.log(hashPropiedadesNotas);
}

// Método que, cada vez que se añade un archivo en la dropzone, crea un nuevo elemento 
// arrastrable al sitio de edición. El nombre se compone para evitar duplicados.
// Método asíncrono, el objeto file no se ha consolidado aún por lo que hay que pasar los parámetros "troceados"
function anadir_item_editor(nombre_archivo, mimetypeArchivo     ) {
    // ext = nombre_archivo.substring(nombre_archivo.lastIndexOf(".") + 1);

    // Se determina la capa que se ha arrastrado a la zona de dropzone.
    var iDiv = document.getElementById(nombre_archivo);

    // Se añade sólo si no existía antes.
    if (!iDiv) {
        iDiv = document.createElement('div');
        iDiv.id = nombre_archivo;

    }

    // Se asigna la clase correcta según el mime
    if (mimetypeArchivo != null) {

        // En función del mime, se asignan los estilos.
        if (mimesVideo.some(v => mimetypeArchivo.includes(v))) {
            iDiv.className = 'item_edicion item_edicion_video';
        } else if (mimesImagenes.some(i => mimetypeArchivo.includes(i))) {
            iDiv.className = 'item_edicion item_edicion_imagen';
        }

        // Se añade una propiedad a la capa: el tipo mime. Esta propiedad estará en la capa original 
        // y en la capa cuando sea arrastrada a la zona de edición.
        iDiv.setAttribute('mime-type', mimetypeArchivo);
    }
    else // Aún no se dispone del mime type.
        iDiv.className = 'item_edicion';


    // Gestión de elemento HOVER
    console.log ("Gestión de elemento HOVER",iDiv);
    iDiv.addEventListener('mouseover', function (evento) {
        muestra_hover(iDiv);
    });
    iDiv.addEventListener('mouseout', function () {
        oculta_hover(iDiv);
    });
    var event = new MouseEvent('mouseover', {
        'view': window,
        'bubbles': true,
        'cancelable': true
    });
    var event2 = new MouseEvent('mouseout', {
        'view': window,
        'bubbles': true,
        'cancelable': true
    });
    iDiv.dispatchEvent(event);
    iDiv.dispatchEvent(event2);
    
      // Fin gestión elemento HOVER 

    // Se añade el elemento al documento.
    document.getElementById('misRecursos').appendChild(iDiv);


    // JQUERY
    $("#sortable").sortable({
        revert: true
    });
}

// Función que muestra un hover específico
function muestra_hover(iDiv) {
    // console.log("HOVER ",iDiv.id);
    // Recupero todos los posibles hovers.


    if (document.getElementById("hover_"+iDiv.id)!=null) {

        var detalle_img = document.getElementById("hover_"+iDiv.id); 
        // console.log("HOVER mostrando", detalle_img);

        detalle_img.style.visibility="visible";
    }
}


// Método que esconde un hover específico
function oculta_hover (iDiv) {
    // console.log("HOVER ",iDiv.id);
    if (document.getElementById("hover_"+iDiv.id)!=null) {

        var detalle_img = document.getElementById("hover_"+iDiv.id);  
        // console.log("HOVER ocultando", detalle_img);

        detalle_img.style.visibility="hidden";

    }
}


// Método que regenera los hovers sólo en la capa "misRecursos". Se regeneran todos 
// en previsión de que haya algún elemento que se arrastre a la zona de edición (se 
// quiere evitar que en la zona de edición haya hovers.
function regenera_hovers ()
{
    // alert ("REGENERANDO H");

    // Recuperamos los elementos de la capa de recursos
    var recursos = document.getElementById("misRecursos").childNodes;

    // Borramos todos los previos
    borra_hovers()


    // console.log ("REGENERANDO H", document.getElementsByClassName("capa_hover"));


    // Recorremos la lista de recursos
    for (i = 0 ; i<recursos.length; i++)
    {
        let nombre_elto = recursos[i].id;

        console.log ('Recorremos la lista de recursos : ',nombre_elto);

        console.log(document.getElementById("img_oculta_" + nombre_elto));
        if (document.getElementById("img_oculta_" + nombre_elto) != null) {
            // console.log("BINGO! SE ENCONTRO img_oculta_" + nombre_elto);
    
            var detalle = document.createElement("div");
            detalle.id = "hover_"+nombre_elto;
            detalle.className="capa_hover_img";

            var detalle_img = document.createElement("img");
            detalle_img.src = document.getElementById("img_oculta_" + nombre_elto).src;
            detalle_img.className ="capa_hover_imagen";
            detalle_img.style.display= "table-row";

            var info =   document.createElement("div");
            info.style.fontFamily="fuente_custom";
            info.style.width="100%";
            info.style.height=""
            info.style.display= "table-row";

            var detalle_img_txt =  document.createElement("span");
            try { detalle_img_txt.innerHTML = nombre_elto.substring(nombre_elto.indexOf("_")+1) + "<br>Duración por defecto: "+duracion_defecto_img+"s."; } catch (exc) {}
            detalle_img_txt.style.alignContent = "left";
            detalle_img_txt.style.position = "relative";
            detalle_img_txt.style.padding ="0px";
            detalle_img_txt.style.paddingRight ="170px";
            detalle_img_txt.style.marginRight ="20px";

            info.append(detalle_img_txt);

            // Se añade la imagen de hover
           // detalle.append(detalle_img_txt);
           detalle.append(info);
           detalle.append(detalle_img);

 
            
            document.getElementById(nombre_elto).appendChild(detalle);


            // Gestión de elemento HOVER
            iDiv = document.getElementById("misRecursos").childNodes[i];
            iDiv.addEventListener('mouseover', function (evento) {
                // console.log("EVENTO",evento.path[1].id);
                // console.log (evento.target);
                if (evento.path) { // Basados en chrome
                    muestra_hover(document.getElementById(evento.path[1].id));
                }
                else if (evento.target != undefined) {// FF
                    try { muestra_hover(document.getElementById(evento.target[1].id)); } catch (error) {}
                }
            });
            iDiv.addEventListener('mouseout', function () {
                oculta_hover(this);
            });
            var event = new MouseEvent('mouseover', {
                'view': window,
                'bubbles': true,
                'cancelable': true
            });
            var event2 = new MouseEvent('mouseout', {
                'view': window,
                'bubbles': true,
                'cancelable': true
            });
            iDiv.dispatchEvent(event);
            iDiv.dispatchEvent(event2);
            // Fin gestión elemento HOVER
        }
        else if (document.getElementById("video_oculto_" + nombre_elto) != null) {
            // console.log ("Es un vídeo: ",nombre_elto);
            var detalle = document.createElement("div");
            detalle.id = "hover_"+nombre_elto;
            detalle.className="capa_hover_vid";

            // Recuperamos la miniatura
            var miniatura;
            var referencia =  document.getElementById(nombre_elto);
            if (referencia.childNodes[0])
            {
                miniatura = referencia.childNodes[0].src;
                // console.log ("Es un vídeo: miniatura ",miniatura);
            }

            var detalle_img = document.createElement("img");
            detalle_img.src = miniatura; 
            detalle_img.className ="capa_hover_imagen";
            detalle_img.style.display= "table-row";

 
            
            var info =   document.createElement("div");
            info.style.fontFamily="fuente_custom";
            info.style.width="100%";
            info.style.height=""
            info.style.display= "table-row";

            var detalle_vid_txt =  document.createElement("span");
            detalle_vid_txt.id = "etiq_video_hover_"+nombre_elto;
            var dur_orig = document.getElementById(nombre_elto).getAttribute("dur_orig");         /* hashDuracionesOriginales.get(nombre_elto) */
            // dur_orig = hashDuracionesOriginales.get("1480958_file_example_AVI_1280_1_5MG.avi");
            // console.log ("Buscando ", nombre_elto, " en ", hashDuracionesOriginales, ":", dur_orig);
/*             console.log ("Buscando duracion________________________________________________");
            console.log (document.getElementById(nombre_elto));
            console.log ("Buscando duracion de ", nombre_elto, "  :", dur_orig); */

            // try { detalle_img_txt.innerHTML = nombre_elto.substring(nombre_elto.indexOf("_")+1) + "<br> Duración: "+dur_orig+" s." } catch (exc) {}
            try { detalle_vid_txt.innerHTML = nombre_elto.substring(nombre_elto.indexOf("_")+1) } catch (exc) {}

            detalle_vid_txt.style.alignContent = "left";
            detalle_vid_txt.style.position = "relative";
            detalle_vid_txt.style.padding ="0px";
            detalle_vid_txt.style.paddingRight ="170px";
            detalle_vid_txt.style.marginRight ="20px";

            info.append(detalle_vid_txt);

            // Se añade la imagen de hover
           // detalle.append(detalle_img_txt);
           detalle.append(info);
           detalle.append(detalle_img);

           document.getElementById(nombre_elto).appendChild(detalle);


            // Gestión de elemento HOVER
            iDiv = document.getElementById("misRecursos").childNodes[i];
            iDiv.addEventListener('mouseover', function (evento) {
                // console.log("EVENTO",evento.path[1].id);
                // console.log (evento.target);
                if (evento.path) { // Basados en chrome
                    muestra_hover(document.getElementById(evento.path[1].id));
                }
                else if (evento.target != undefined) {// FF
                    try { muestra_hover(document.getElementById(evento.target[1].id)); } catch (error) {}
                }
            });
            iDiv.addEventListener('mouseout', function () {
                oculta_hover(this);
            });
            var event = new MouseEvent('mouseover', {
                'view': window,
                'bubbles': true,
                'cancelable': true
            });
            var event2 = new MouseEvent('mouseout', {
                'view': window,
                'bubbles': true,
                'cancelable': true
            });
            iDiv.dispatchEvent(event);
            iDiv.dispatchEvent(event2);
            // Fin gestión elemento HOVER        }
        }
    }


}


function borra_hovers () {
    // Ocultamos todos los posibles hovers.
    for (i=0; i<document.getElementsByClassName ("capa_hover").length; i++)
    {
        console.log("borrando ", document.getElementsByClassName ("capa_hover")[i]);
        document.getElementsByClassName ("capa_hover")[i].remove();
    }

    // Ocultamos todos los posibles hovers.
    for (i = 0; i < document.getElementsByClassName("capa_hover_img").length; i++) {
        console.log("borrando ", document.getElementsByClassName("capa_hover_img")[i]);
        try { document.getElementsByClassName("capa_hover_img")[i].style.visibility = "hidden"; } catch (error) { console.log(error); }
    }

    // Ocultamos todos los posibles hovers.
    for (i = 0; i < document.getElementsByClassName("capa_hover_vid").length; i++) {
        console.log("borrando ", document.getElementsByClassName("capa_hover_vid")[i]);
        try { document.getElementsByClassName("capa_hover_vid")[i].style.visibility = "hidden"; } catch (error) { console.log(error); }
    } 
}






///////////////////////// Gestión fetch: envío de formulario
function lanzaFetch() {


    actualiza_barra_progreso(1);

    // Verificamos varios intentos seguidos de fetch
    if (en_curso != 1) {

        // Recuperamos los elementos que hay en el área de edición
        let elementosEdicion = document.getElementById("miZonaEdicion").childNodes;

        if (elementosEdicion.length != undefined && elementosEdicion.length > 0) {
            // Se indica que la operación ha comenzado
            en_curso = 1;

            // Se actualiza la frecuencia de refresco
            segundos_actualizacion = 1;


            // Variable donde se almacenará el contenido de la petición
            var datosJSON = [];

            // Duración final del vídeo.
            var duracion_total = 0;

            // Recorremos el área de edición para mostrar los items EN ORDEN
            // Originalmente se excluía el primer elemento, que era un texto en blanco para forzar el tamaño del área
            for (var ele = 0; ele < elementosEdicion.length; ele++) {
                let nombre = hashPropiedades.get(elementosEdicion[ele].id).nombre_original;
                let duracion = hashPropiedades.get(elementosEdicion[ele].id).duracion;
                let desde = Math.round(hashPropiedades.get(elementosEdicion[ele].id).inicio); // En caso de que llegue con decimales.
                let hasta = Math.round(hashPropiedades.get(elementosEdicion[ele].id).fin); // En caso de que llegue con decimales.
                let notas = []; // JSON con los atributos de las notas en este vídeo


                // let inicio_seccion = duracion_total; 

                if (duracion == undefined) duracion = 0;
                if (Number.isNaN(duracion)) duracion = 0;
                if (Number.isNaN(hasta)) hasta = 0;
                if (Number.isNaN(desde)) desde = 0;

                console.log("duracion = ", duracion * step, ", hasta = ", hasta, ", desde = ", desde);

                duracion_total = duracion_total + duracion * step + (hasta - desde);

                // let fin_seccion = duracion_total;

                console.log(" duracion_total = " + duracion_total, ", hasta = ", hasta, ", desde = ", desde);
                console.log(" hashPropiedadesNotas = ", hashPropiedadesNotas);

                // Si estamos en el primer elemento, procesamos las notas.
                if (ele == 0) {
                    // Recuperamos las notas según aparecen en el DOM
                    let marco_notas = document.getElementById("marco_oculto_notas");

                    if (marco_notas != null && marco_notas != undefined) {
                        let notas_DOM = marco_notas.childNodes;
                        console.log("notas = ", notas_DOM);

                        // Recorremos las notas en orden
                        for (nota = 0; nota < notas_DOM.length; nota++) {
                            // Recuperamos el id de la nota n-ésima
                            let id_nota = notas_DOM[nota].id;

                            // Para ese id recogemos el inicio y fin
                            var inicio_nota = hashPropiedadesNotas.get("ini_" + id_nota);
                            var fin_nota = hashPropiedadesNotas.get("fin_" + id_nota); // Recuperamos el par ini_ fin_



                            /*
                            [[Entries]]
                            0: {"ini_NOTA_1640544377462.6" => "0"}
                            1: {"fin_NOTA_1640544377462.6" => 1.7976931348623157e+308}
                            2: {"ini_NOTA_1640544380174" => "0"}
                            3: {"fin_NOTA_1640544380174" => 1.7976931348623157e+308}
                            size: 4
                            */
                            /*// Recorremos las notas para anexarlas sólo en el primer elemento (serán únicas en el vídeo)
                            for (nota = 0; nota < hashPropiedadesNotas.size  ; nota ++) 
                            {
                                let id_posicion = Array.from(hashPropiedadesNotas.keys())[nota];
                                let rango = id_posicion.substr(0,3);
            
                                if (rango == "ini") {
                                    var id_nota = id_posicion.substr(4);
            
                                    var  inicio_nota =  hashPropiedadesNotas.get(id_posicion);
                                    var  fin_nota =  hashPropiedadesNotas.get("fin_"+id_nota); // Recuperamos el par ini_ fin_*/

                            console.log("VERIFICANDO NOTA ", id_nota);
                            console.log(" inicio_nota = " + inicio_nota, ", fin_nota = ", fin_nota);

                            id_nota = id_nota.substr(5);
                            console.log("La nota ", id_nota, " pertenece a este rango");
                            console.log("Inicio = ", inicio_nota, ", fin =  ", fin_nota);

                            // Recuperamos la información de la nota, ya sea del recuadro de texto o de 
                            let texto_nota = "";;
                            let estilo_nota = "";
                            let tamano_nota = "";
                            let color_nota = "";
                            let img_nota = "";

                            if (document.getElementById("recuadro" + id_nota) != undefined) {
                                texto_nota = document.getElementById("recuadro" + id_nota).value;
                                estilo_nota = document.getElementById("recuadro" + id_nota).classList.toString();
                                tamano_nota = document.getElementById("recuadro" + id_nota).style.fontSize;
                                color_nota = document.getElementById("recuadro" + id_nota).style.color;
                            }
                            else  // Si no encuentra el recuadro (se ha quitado el input text para meter una imagen)...
                            {
                                img_nota = document.getElementById("NOTA_" + id_nota + "_imagen_nota").src;
                                img_nota = img_nota.substring(img_nota.lastIndexOf("/") + 1); // Se deja sólo el nombre del archivo.

                                // Se incluye el tamaño de la nota original en el JSON
                               //  tamano_nota = document.getElementById("NOTA_" + id_nota + "_imagen_nota").getAttribute("tam_original");

                                console.log ("FETCH: tamaño original =", document.getElementById("NOTA_" + id_nota + "_imagen_nota").naturalWidth + "x" + document.getElementById("NOTA_" + id_nota + "_imagen_nota").naturalHeight);
                                tamano_nota = document.getElementById("NOTA_" + id_nota + "_imagen_nota").naturalWidth + "x" + document.getElementById("NOTA_" + id_nota + "_imagen_nota").naturalHeight

                                // Como estilo se incluye la altura máxima
                                estilo_nota = document.getElementById("NOTA_" + id_nota + "_imagen_nota").style.maxHeight;
                                estilo_nota = estilo_nota.substring(0, estilo_nota.indexOf("px"));

                            }

                            notas.push({
                                "inicio": inicio_nota,
                                "fin": fin_nota,
                                "texto": texto_nota,
                                "posX": document.getElementById("NOTA_" + id_nota).style.left,
                                "posY": document.getElementById("NOTA_" + id_nota).style.top,
                                "estilo": estilo_nota,
                                "tam": tamano_nota,
                                "color": color_nota,
                                "img_nota": img_nota
                            });

                            // }
                        }
                    }
                }

                datosJSON.push({
                    "id": ele - 1,
                    "nombre": nombre,
                    "duracion": duracion,
                    "desde": desde,
                    "hasta": hasta,
                    "precision": step,
                    "notas": notas
                });


            }

            console.log("JSON = ", JSON.stringify(datosJSON));


            fetch('/creaVid', {
                method: 'POST',
                body: JSON.stringify(datosJSON),
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'authHeader',
                    /* 'Content-Disposition': 'attachment',
                    'filename': "'filename.mp4'" */
                }
            })
                .then((data) => data.blob())
                .then(response => {
                    // https://www.codegrepper.com/code-examples/javascript/fetch+download+blob+file
                    // console.log(response.type);


                    const dataType = response.type;
                    const binaryData = [];
                    binaryData.push(response);
                    const downloadLink = document.createElement('a');
                    downloadLink.href = window.URL.createObjectURL(new Blob(binaryData, { type: dataType }));
                    downloadLink.setAttribute('download', 'fichero.mp4');
                    document.body.appendChild(downloadLink);
                    downloadLink.click();
                    downloadLink.remove();

                    en_curso = 0;
                    // Se actualiza la frecuencia de refresco
                    // actualiza_barra_progreso(100000000);
                })

        }
        else // No hay elementos en la zona de edición
        {
            alert("No hay clips en la zona de edición :( ");
        }
    }
    else // Hay otra llamada en curso
    {
        alert("No se permiten llamadas duplicadas.");
    }

    // Se actualiza la frecuencia de refresco
    actualiza_barra_progreso(1000);
    
}

// Método para volver a ocultar una capa.
function oculta_capa(id_capa) {
    // Se actualiza en las cookies el valor de todas la notas
    almacena_notas_cookies ();

    if (keep == 0) {
        document.getElementById(id_capa).style.visibility = 'hidden';
        document.getElementById(id_capa).style.zIndex = 0;
        notas = document.getElementsByTagName("canvas_nota");

        // Eliminamos una posible imagen temporal que servía de fondo para la edición de notas.
        if (document.getElementById("img_temporal") != null)
            document.getElementById("img_temporal").remove();

        oculta_todas_notas()
    }
    else
        keep = 0;
}


// Método para ocultar todas las capas de notas
function oculta_todas_notas() {
    let notas = document.getElementsByClassName("canvas_nota");


    if (notas != null) {
        for (i = 0; i < notas.length; i++) {
            notas[i].style.visibility = 'hidden';
            notas[i].style.zIndex = 0;
        }
    }
}


// Busca la miniatura
function regenera_miniatura(idSesion, id_imagen) {
    // let actual = document.getElementById('misRecursos').childNodes[document.getElementById('misRecursos').childNodes.length - 1];
    let actual = document.getElementById(id_imagen);
    //console.log("actual = ", actual);

    var id_img = getBase() + idSesion + '/miniaturas/thumb_' + id_imagen;
    var tipo = "img"; // Indica si el tipo de contenido es imagen o vídeo

    // TODO: revisar captura de mime type para relacionar con el servidor.
    // if (id_imagen.endsWith(".mp4") || id_imagen.endsWith(".mkv") || id_imagen.endsWith(".avi")) {
    //     id_img += ".jpg";
    //     tipo = "vid";
    // }

    // Recupera el mime type
    let mime_type = actual.getAttribute("mime-type");

    // Si es un vídeo, hay que buscar el .jpg
    if (mimesVideo.some(v => mime_type.includes(v))) {
        console.log ("regenera_miniatura. Es un vídeo");
        id_img += ".jpg";
        tipo = "vid";
    }

    //console.log('regenera_miniatura: img = ', id_img);
    //console.log('regenera_miniatura: id_imagen = ', id_imagen);

    // let texto_alt = id_imagen + '.Haz click para fijar la duración.';

    actual.innerHTML = "<img  class='clase_miniatura' src='" + id_img + "' img_original='" + id_imagen + "' tipo_contenido='" + tipo + "' onError='miniatura_esperando(\"" + id_imagen + "\"); setTimeout(() => {regenera_miniatura(\"" + idSesion + "\", \"" + id_imagen + "\")}, 500);'>";
}



// Método que actualizará el valor del thumbnail mientras se espera al servidor, controlando el total de reintentos.
function miniatura_esperando(id_imagen) {
    // Recuperamos el último item
    // let actual = document.getElementById('misRecursos').childNodes[document.getElementById('misRecursos').childNodes.length - 1];
    let actual = document.getElementById(id_imagen);

    if (valor = hashReintentos.get(id_imagen)) {
        hashReintentos.set(id_imagen, valor + 1);
        //console.log("reintento ", id_imagen, ", ", valor);
        // TODO: imponer un máximo de reintentos.
    }
    else {
        //console.log("primer reintento");
        hashReintentos.set(id_imagen, 1);
    }

    actual.innerHTML = "...";
}



// Función llamada cuando se añade un nuevo elemento en el área de edición.
function recalcula_propiedades() {
    console.log('Recalculando propiedades...');
    


    // Método para recuperar los elementos que hay en el área de edición
    let elementosEdicion = document.getElementById("miZonaEdicion").childNodes;
    // console.log (elementosEdicion);
    let timeStampInMs;
    let nombre_original;
    let mime_objeto;
    let duracion_video;
    let tmpPropiedades;


    // Recorremos el área de edición para mostrar los items EN ORDEN
    // Originalmente se excluía el primer elemento, que era un texto en blanco para forzar el tamaño del área
    for (let ele = 0; ele < elementosEdicion.length; ele++) {
        nombre_original = elementosEdicion[ele].id;

        // Si hay un cambio y el elemento no tiene ID, se añade. 
        if (!nombre_original.startsWith("ID")) {
            //console.log('El elemento NO estaba antes, generando sus propiedades');
            timeStampInMs = window.performance && window.performance.now && window.performance.timing && window.performance.timing.navigationStart ? window.performance.now() + window.performance.timing.navigationStart : Date.now();

            mime_objeto = elementosEdicion[ele].getAttribute("mime-type");
            // alert(mime_objeto);

            // Se le da un ID válido
            elementosEdicion[ele].id = "ID" + timeStampInMs;
            elementosEdicion[ele].setAttribute("data-nombre", nombre_original);
            let texto_alt = nombre_original.substring(nombre_original.indexOf("_")+1) +'. Cambia su posición o haz click para fijar su duración.';
            elementosEdicion[ele].setAttribute("alt",texto_alt);
            elementosEdicion[ele].setAttribute("title",texto_alt);

            tmpPropiedades = new Map();

            // En función del mime, se asignan los atributos
            if (mimesVideo.some(v => mime_objeto.includes(v))) {
                let duracion = get_duracion(nombre_original);
                // alert(duracion);

                tmpPropiedades.set(elementosEdicion[ele].id, { nombre_original: nombre_original, duracion_video: duracion, inicio: 0, fin: duracion });


            }
            else if (mimesImagenes.some(i => mime_objeto.includes(i)))  // Si es imagen, se asigna la duración por defecto.
            {
                tmpPropiedades.set(elementosEdicion[ele].id, { nombre_original: nombre_original, duracion: duracion_defecto_img });
            }

            // Se añade el evento de click que abrirá la ventana emergente.
            elementosEdicion[ele].onclick = function () { ventana_emergente("ID" + timeStampInMs) };

            // Añadimos la hash temporal a la hash de propiedades
            // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Map
            hashPropiedades = new Map([...hashPropiedades, ...tmpPropiedades])
        }
        else {
            console.log('El elemento ya estaba antes, recuperando sus propiedades', elementosEdicion[ele]);
            let texto_alt = nombre_original.substring(nombre_original.indexOf("_")+1) +'. Cambia su posición o haz click para fijar su duración.';
            elementosEdicion[ele].childNodes[0].setAttribute("alt",texto_alt);
            elementosEdicion[ele].childNodes[0].setAttribute("title",texto_alt);

        }
    }


    recalcula_propiedades_cookies();

    regenera_hovers();

}








// Método que recalcula el valor de las cookies de los elementos de la zona de edición
function recalcula_propiedades_cookies ()
{
    // Gestión de cookies.
    // Se borran todas las cookies de este tipo.
    deleteGrupoCookies("elementosEdicion");

    // Se recorre para completar las cookies.
    let elementosEdicionFinal = document.getElementById("miZonaEdicion").childNodes;

    // console.log(hashPropiedades)

    // Recorremos el área de edición para almacenar su valor actual
    for (let ele = 0; ele < elementosEdicionFinal.length; ele++) {
        document.cookie = "elementosEdicion."+ele+".nombre = " + elementosEdicionFinal[ele].id + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";  
        document.cookie = "elementosEdicion."+ele+".nombre_original = " +  hashPropiedades.get (elementosEdicionFinal[ele].id).nombre_original  + "; expires=Thu, 1 Jan 2122 12:00:00 UTC"; // elementosEdicionFinal[ele].getAttribute("data-nombre") ;  
        document.cookie = "elementosEdicion."+ele+".duracion = " + hashPropiedades.get (elementosEdicionFinal[ele].id).duracion + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";  
        document.cookie = "elementosEdicion."+ele+".duracion_video = " + hashPropiedades.get (elementosEdicionFinal[ele].id).duracion_video + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";  
        document.cookie = "elementosEdicion."+ele+".inicio = " +hashPropiedades.get (elementosEdicionFinal[ele].id).inicio + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";  
        document.cookie = "elementosEdicion."+ele+".fin = " + hashPropiedades.get (elementosEdicionFinal[ele].id).fin + "; expires=Thu, 1 Jan 2122 12:00:00 UTC";  
    }
    // FIN gestión cookies
}



// Método que se llama para limitar el tamaño del recuadro de texto al tamaño de la capa contenedora.
// Esto se hacer porque al escribir en el recuadro y exceder los límites de la capa se desplazaban todos los 
// demás elementos a la izquierda.
function limita_ancho(elemento) {
    var posicion = elemento.getBoundingClientRect();
    var margen_derecho_contenedor = document.getElementById("marco_oculto_notas").getBoundingClientRect().right;
    var margen_inferior_contenedor = document.getElementById("marco_oculto_notas").getBoundingClientRect().bottom;

    elemento.style.maxWidth = eval(margen_derecho_contenedor - posicion.left) + "px";
    elemento.style.width = eval(margen_derecho_contenedor - posicion.left) + "px";
    // console.log("margen_inferior_contenedor = ",margen_inferior_contenedor, ", posicion = ",posicion);

    // Para evitar problemas en la edición, se limita la posición inferior.
    if (margen_inferior_contenedor < posicion.bottom) {
        if (elemento.readOnly == false)
            toastr.success('No es posible añadir texto fuera de la zona de edición');

        elemento.readOnly = true;
    }
    else
        elemento.readOnly = false;
}



new Sortable(misRecursos, {
    group: {
        name: 'shared',
        pull: 'clone',
        put: false // Do not allow items to be put into this list
    },
    animation: 150,
    sort: false // To disable sorting: set sort to false
});

new Sortable(miZonaEdicion, {
    group: 'shared',
    animation: 150
});



new Sortable(papelera, {
    group: 'shared',
    animation: 150
});


// Variable de observación que se mantendrá sobre la capa de carga de imágenes y vídeos.
// Cuando se finalice la carga de un elemento, se actualizarán sus dimensiones en función
// de su duración (fija si es imagen, la del vídeo si es un vídeo).
var observer = new MutationObserver(function (mutations) {
    //console.log("mutación!");
    // Se recorre la mutación observada
    for (m in mutations) {
        tipo_anadido = mutations[m].addedNodes[0];
        if (tipo_anadido && tipo_anadido.className && tipo_anadido.className == "item_edicion item_edicion_imagen") // Si es imagen
        {
            // Se almacena en la hash la duración original
            // console.log("Mutation. Duración de " + tipo_anadido.id + "= " + duracion_defecto_img * step);
            hashDuracionesOriginales.set(tipo_anadido.id, duracion_defecto_img * step);

            // Se ajusta el ancho de la miniatura
            document.getElementById(tipo_anadido.id).style.maxWidth = (ratio_visualizacion * 100) + "px"; // Ratio testado : 45px = 10segundos.

            
            // Se hace visible la imagen
            document.getElementById(tipo_anadido.id).style.visibility = "visible";
        }
        else if (tipo_anadido && tipo_anadido.className && tipo_anadido.className == "item_edicion item_edicion_video") // Si es vídeo
        {
            let duracion = 0;

            fetch("/getInfoVid/" + tipo_anadido.id)
                .then(response => response.text())
                .then((response) => {
                    duracion = response.substr(response.indexOf(",") + 1);
                    clip = response.substr(0, response.indexOf(","));

                    // Se almacena en la hash la duración original
                    hashDuracionesOriginales.set(clip, duracion * step);

                    console.log ("SETTING duracion________________________________________________", duracion, ".", clip);
                    try { document.getElementById("etiq_video_hover_"+clip).innerHTML = clip.substring(clip.indexOf('_')+1) + "<br> Duración: "+Number(duracion).toFixed(2)+" s." } catch (exc) {console.log (exc);}

                    // OK console.log("Mutation. VIDEO " + clip + ", duracion = " + duracion);

                    // OK console.log("Mutation. document.getElementById : "+ document.getElementById("video_oculto_"+clip));
                    document.getElementById(clip).style.maxWidth = duracion * 10 * ratio_visualizacion + "px";
                    document.getElementById(clip).style.minWidth = duracion * 10 * ratio_visualizacion + "px";

                    
                    // Se hace visible la imagen
                    document.getElementById(clip).style.visibility = "visible";
                })
                .catch(err => console.log(err))


        }
        // regenera_hovers(); // Cuando se añade un nuevo elemento, se regeneran los hovers
    }
});

function formatNumber(num) {
    return num.toString().replace(/(\d)(?=(\d{3})+(?!\d))/g, '$1,')
  }

observer.observe(document.getElementById("misRecursos"), { attributes: false, childList: true, characterData: false, subtree: true });

