// Función para recuperar las variables. A esta función sólo se le llamará si se recarga la página.
function get_variables_edicion() {
    // console.log("Carga inicial. Se recuperan las posibles cookies de la sesión");

    var listaArchivos = getCookie("elementosEdicion");
    // alert ("archivos = ",elementosEdicion);

    // En caso de un F5, limpiamos los posibles contenidos viejos de la tabla
    hashPropiedades.clear();

    /*
    // Recorremos el área de edición para almacenar su valor actual
    for (let ele = 0; ele < elementosEdicionFinal.length; ele++) {
        document.cookie = "elementosEdicion."+ele+".nombre = " + elementosEdicionFinal[ele].id;  
        document.cookie = "elementosEdicion."+ele+".nombre_original = " +  hashPropiedades.get (elementosEdicionFinal[ele].id).nombre_original; // elementosEdicionFinal[ele].getAttribute("data-nombre") ;  
        document.cookie = "elementosEdicion."+ele+".duracion = " + hashPropiedades.get (elementosEdicionFinal[ele].id).duracion;  
        document.cookie = "elementosEdicion."+ele+".duracion_video = " + hashPropiedades.get (elementosEdicionFinal[ele].id).duracion;  
        document.cookie = "elementosEdicion."+ele+".inicio = " +hashPropiedades.get (elementosEdicionFinal[ele].id).inicio;  
        document.cookie = "elementosEdicion."+ele+".fin = " + hashPropiedades.get (elementosEdicionFinal[ele].id).fin;  
    }
    // FIN gestión cookies
}
    */
    // 1 copia elemento desde misrecursos a mizonaedicion
    // 2 llamar a recalcula_propiedades para que le dé el id 

    // Recorro los recursos
    var recursos = document.getElementById("misRecursos").childNodes;

    // for (i = 0; i < recursos.length; i++) {
    //     console.log("getVariablesEdicion  =", recursos[i]);
    // }

    // Recojo las cookies
    var fin = false;
    var indice = 0;
    var capa;

    while (!fin) {
        var elto = getCookie("elementosEdicion." + indice + ".nombre"); // nombre_original
        // console.log("> Se intenta recuperar la cookie = elementosEdicion." ,indice , ".nombre_original");
        console.log("   Cookie recuperada = ", elto);

        if (elto == null || elto == "" || elto == undefined) {
            fin = true;
        } else {
            // De la cookie se recupera la capa original
            let nombre_original = getCookie("elementosEdicion." + indice + ".nombre_original"); 
            let capa_original = document.getElementById(nombre_original) ;
            
            // Si la capa existe, había una edición anterior
            if (capa_original != undefined)
            {
                let capa = capa_original.cloneNode(2);
                // console.log("       Recuperada capa clonada = ", capa);

                // Nos aseguramos de que es visible
                capa.style.visibility = "visible";

                // Se añade la capa a la zona de edición
                document.getElementById("miZonaEdicion").appendChild(capa);

                indice++;
            }
            else // Si hay alguna capa que no se encuentra, se considera que es un resto de una edición anterior
            {
                indice++;

            }
        }
    }        
    
    // Se asigna el ID y se actualiza la tabla de propiedades desde las cookies.
    recalcula_propiedades_reload();
    
}



// Función para recuperar una cookie específica
// https://www.w3schools.com/js/js_cookies.asp
function getCookie(cname) {
    let name = cname + "=";
    let decodedCookie = decodeURIComponent(document.cookie);
    let ca = decodedCookie.split(';');
    for (let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

// Función para borrar un grupo de cookies específicas
function deleteGrupoCookies(prefijo) {
    let decodedCookie = decodeURIComponent(document.cookie);
    let ca = decodedCookie.split(';');
    for (let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(prefijo) == 0) {
            // return c.substring(name.length, c.length);
            // console.log ("deleteGrupoCookies: borrando cookie",c.substring(0, prefijo.length));
            document.cookie = c + "=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";

        }
    }
}


// Función llamada para recalcular las propiedades por recarga, recuperando los datos de las posibles 
// cookies que haya.
function recalcula_propiedades_reload() {
    console.log('Recalculando propiedades tras recarga...');

    // Método para recuperar los elementos que hay en el área de edición
    let elementosEdicion = document.getElementById("miZonaEdicion").childNodes;
     console.log (elementosEdicion);
    let nombre_capa;
    let mime_objeto;
    let tmpPropiedades = new Map();
    let timeStampInMs;

    // Recorremos el área de edición para mostrar los items EN ORDEN
    // Originalmente se excluía el primer elemento, que era un texto en blanco para forzar el tamaño del área
    for (let ele = 0; ele < elementosEdicion.length; ele++) {
        nombre_capa = elementosEdicion[ele].id;

        console.log('El elemento NO estaba antes, generando sus propiedades');
        // Se calcula el timestamp para darle un id único 
        timeStampInMs = window.performance && window.performance.now && window.performance.timing && window.performance.timing.navigationStart ? window.performance.now() + window.performance.timing.navigationStart : Date.now();

        // Se añade un random para evitar bug de firefox https://bugzilla.mozilla.org/show_bug.cgi?id=363258
        let id_final = "ID" + timeStampInMs + Math.random();

        // Se recupera el valor del mime-type para determinar si es vídeo o imagen.
        mime_objeto = elementosEdicion[ele].getAttribute("mime-type");

        // Se le da un ID válido
        console.log ("ASIGNANDO TIMESTAMP", Date.now()+Math.random());
        elementosEdicion[ele].id = id_final;
        elementosEdicion[ele].setAttribute("data-nombre", nombre_capa);       

        // Se le da un ID válido
        let texto_alt = "Fichero '" + nombre_capa.substring(nombre_capa.indexOf('_')+1) +"'.<br/>Cambia su posición o haz click para fijar su duración.";
        elementosEdicion[ele].setAttribute("alt",texto_alt);
        elementosEdicion[ele].setAttribute("title",texto_alt);



        // En función del mime, se asignan los atributos
        if (mimesVideo.some(v => mime_objeto.includes(v))) {
            let _duracion = getCookie("elementosEdicion." + ele + ".duracion_video");
            let _inicio = getCookie("elementosEdicion." + ele + ".inicio");
            let _fin = getCookie("elementosEdicion." + ele + ".fin");
            // alert(duracion);

            // Si el inicio no está definido, se considera 0
            if (_inicio == undefined)
                _inicio = 0 ;

            // Se almacenan las propiedades en la hash.
            tmpPropiedades.set(elementosEdicion[ele].id, { nombre_original: nombre_capa, duracion_video: _duracion, inicio: _inicio, fin: _fin });


            // Se recalcula el nuevo ancho en el área de edición
            document.getElementById(id_final).style.minWidth = eval(_fin - _inicio) / granularidad * 10 * ratio_visualizacion + "px";
            document.getElementById(id_final).style.maxWidth = eval(_fin - _inicio) / granularidad * 10 * ratio_visualizacion + "px";


        }
        else if (mimesImagenes.some(i => mime_objeto.includes(i)))  // Si es imagen, se asigna la duración por defecto.
        {
            // Se recupera la propiedad "duración" de la imagen.
            let _duracion = getCookie("elementosEdicion." + ele + ".duracion");

            // Se almacenan las propiedades en la hash.
            tmpPropiedades.set(elementosEdicion[ele].id, { nombre_original: nombre_capa, duracion: _duracion });

            // Se actualiza el ancho en la zona de edición
            document.getElementById(id_final).style.minWidth = _duracion* 10 * ratio_visualizacion + "px";
            document.getElementById(id_final).style.maxWidth = _duracion* 10 * ratio_visualizacion + "px"; 

        }

        // Se añade el evento de click que abrirá la ventana emergente.
        elementosEdicion[ele].onclick = function () { ventana_emergente(id_final) };

        // Añadimos la hash temporal a la hash de propiedades
        // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Map
        hashPropiedades = new Map([...hashPropiedades, ...tmpPropiedades])
    }
}
